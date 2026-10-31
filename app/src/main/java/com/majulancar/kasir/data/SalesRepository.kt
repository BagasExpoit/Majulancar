package com.majulancar.kasir.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import java.util.UUID

/**
 * Local sales ledger (omzet). Replaces the web app's /api/sales endpoint
 * and Upstash/JSON-file store: every completed payment is appended to an
 * encrypted list in [SecurePrefs] and never leaves the device.
 */
class SalesRepository(context: Context) {

    private val prefs = SecurePrefs.get(context)

    fun all(): List<Sale> = load()

    /** Records a finished sale and returns it. [cashReceived] is only used for [PaymentMethod.CASH]. */
    fun record(
        items: List<CartItem>,
        method: PaymentMethod,
        cashReceived: Long? = null
    ): Sale {
        val total = items.sumOf { it.subtotal }
        val isCash = method == PaymentMethod.CASH
        val sale = Sale(
            id = newId(),
            items = items.map { it.copy() },
            total = total,
            createdAtEpochMillis = System.currentTimeMillis(),
            paymentMethod = method,
            cashReceived = if (isCash) cashReceived else null,
            change = if (isCash && cashReceived != null) cashReceived - total else null
        )
        save(load() + sale)
        return sale
    }

    fun summary(now: Long = System.currentTimeMillis()): SalesSummary {
        val sales = load()
        val startOfToday = startOfDay(now)
        val today = sales.filter { it.createdAtEpochMillis >= startOfToday }
        val qris = sales.filter { it.paymentMethod == PaymentMethod.QRIS }
        val cash = sales.filter { it.paymentMethod == PaymentMethod.CASH }
        return SalesSummary(
            totalOmzet = sales.sumOf { it.total },
            totalTransaksi = sales.size,
            omzetHariIni = today.sumOf { it.total },
            transaksiHariIni = today.size,
            omzetQris = qris.sumOf { it.total },
            omzetTunai = cash.sumOf { it.total },
            transaksiQris = qris.size,
            transaksiTunai = cash.size,
            riwayat = sales.asReversed().take(HISTORY_LIMIT)
        )
    }

    fun reset() {
        prefs.edit().remove(KEY).apply()
    }

    private fun newId(): String {
        val suffix = UUID.randomUUID().toString().replace("-", "").take(6).uppercase(Locale.ROOT)
        return "TRX-${System.currentTimeMillis()}-$suffix"
    }

    private fun startOfDay(now: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = now
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    private fun save(sales: List<Sale>) {
        val arr = JSONArray()
        for (s in sales) arr.put(toJson(s))
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private fun load(): List<Sale> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val out = ArrayList<Sale>(arr.length())
            for (i in 0 until arr.length()) out.add(fromJson(arr.getJSONObject(i)))
            out
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun toJson(s: Sale): JSONObject {
        val o = JSONObject()
        o.put("id", s.id)
        o.put("total", s.total)
        o.put("createdAt", s.createdAtEpochMillis)
        o.put("paymentMethod", s.paymentMethod.name)
        val cashReceived = s.cashReceived
        if (cashReceived != null) o.put("cashReceived", cashReceived)
        val change = s.change
        if (change != null) o.put("change", change)
        val items = JSONArray()
        for (line in s.items) {
            val io = JSONObject()
            io.put("id", line.id)
            io.put("name", line.name)
            io.put("price", line.price)
            io.put("quantity", line.quantity)
            items.put(io)
        }
        o.put("items", items)
        return o
    }

    private fun fromJson(o: JSONObject): Sale {
        val itemsJson = o.getJSONArray("items")
        val items = ArrayList<CartItem>(itemsJson.length())
        for (i in 0 until itemsJson.length()) {
            val io = itemsJson.getJSONObject(i)
            items.add(
                CartItem(
                    id = io.getString("id"),
                    name = io.getString("name"),
                    price = io.getLong("price"),
                    quantity = io.getInt("quantity")
                )
            )
        }
        return Sale(
            id = o.getString("id"),
            items = items,
            total = o.getLong("total"),
            createdAtEpochMillis = o.getLong("createdAt"),
            paymentMethod = PaymentMethod.valueOf(o.getString("paymentMethod")),
            cashReceived = if (o.has("cashReceived")) o.getLong("cashReceived") else null,
            change = if (o.has("change")) o.getLong("change") else null
        )
    }

    private companion object {
        const val KEY = "sales"
        const val HISTORY_LIMIT = 100
    }
}
