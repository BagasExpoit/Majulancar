package com.majulancar.kasir.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.json.JSONArray
import org.json.JSONObject

/**
 * The on-device shopping cart. Persisted in [SecurePrefs] (encrypted) so
 * it survives process death, and exposed as [LiveData] so the cart tab and
 * the bottom-nav badge update immediately. Replaces the web app's
 * localStorage cart. All calls must happen on the main thread.
 */
class CartRepository(context: Context) {

    private val prefs = SecurePrefs.get(context)
    private val _items = MutableLiveData<List<CartItem>>(load())

    val items: LiveData<List<CartItem>> = _items

    val current: List<CartItem>
        get() = _items.value.orEmpty()

    val total: Long
        get() = current.sumOf { it.subtotal }

    fun add(item: MenuItem, quantity: Int) {
        if (quantity <= 0) return
        val list = current.toMutableList()
        val index = list.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            val existing = list[index]
            list[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            list.add(CartItem(item.id, item.name, item.price, quantity))
        }
        commit(list)
    }

    /** Changes the quantity by [delta]; never goes below 1 (use [remove] to delete). */
    fun updateQuantity(id: String, delta: Int) {
        commit(current.map {
            if (it.id == id) it.copy(quantity = maxOf(1, it.quantity + delta)) else it
        })
    }

    fun remove(id: String) {
        commit(current.filter { it.id != id })
    }

    fun clear() {
        commit(emptyList())
    }

    private fun commit(list: List<CartItem>) {
        prefs.edit().putString(KEY, toJson(list)).apply()
        _items.value = list
    }

    private fun load(): List<CartItem> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val out = ArrayList<CartItem>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    CartItem(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        price = o.getLong("price"),
                        quantity = o.getInt("quantity")
                    )
                )
            }
            out
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun toJson(list: List<CartItem>): String {
        val arr = JSONArray()
        for (item in list) {
            val o = JSONObject()
            o.put("id", item.id)
            o.put("name", item.name)
            o.put("price", item.price)
            o.put("quantity", item.quantity)
            arr.put(o)
        }
        return arr.toString()
    }

    private companion object {
        const val KEY = "cart"
    }
}
