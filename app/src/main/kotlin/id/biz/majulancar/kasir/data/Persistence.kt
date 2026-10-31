package id.biz.majulancar.kasir.data

import id.biz.majulancar.kasir.core.SecureStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/** [quarantined] = berkas lama tidak terbaca (rusak / kunci berubah) dan telah diamankan. */
class LoadResult<T>(val value: T, val quarantined: Boolean)

private const val FORMAT_VERSION = 1
private const val MAX_SALE_ITEMS = 200
private const val MAX_PRICE = 1_000_000_000L

/**
 * Riwayat omzet, tersimpan terenkripsi (AES-256-GCM, kunci di Android Keystore).
 *
 * IOException (kegagalan I/O sementara) DILEMPAR ke pemanggil dan berkas tidak disentuh;
 * hanya data yang benar-benar rusak/tidak bisa didekripsi yang dikarantina.
 */
class SalesRepository(private val store: SecureStore) {

    suspend fun load(): LoadResult<List<Sale>> = withContext(Dispatchers.IO) {
        try {
            val bytes = store.read(FILE) ?: return@withContext LoadResult(emptyList<Sale>(), false)
            LoadResult(decode(String(bytes, Charsets.UTF_8)), false)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            store.quarantine(FILE)
            LoadResult(emptyList<Sale>(), true)
        }
    }

    suspend fun save(sales: List<Sale>) = withContext(Dispatchers.IO) {
        store.write(FILE, encode(sales))
    }

    private fun encode(sales: List<Sale>): ByteArray {
        val arr = JSONArray()
        for (s in sales) {
            val o = JSONObject()
            o.put("id", s.id)
            o.put("t", s.createdAtMillis)
            o.put("m", if (s.method == PaymentMethod.QRIS) "qris" else "cash")
            o.put("total", s.total)
            if (s.cashReceived != null) o.put("cash", s.cashReceived)
            if (s.change != null) o.put("chg", s.change)
            val items = JSONArray()
            for (i in s.items) {
                val io = JSONObject()
                io.put("id", i.id)
                io.put("n", i.name)
                io.put("p", i.price)
                io.put("q", i.quantity)
                items.put(io)
            }
            o.put("items", items)
            arr.put(o)
        }
        val root = JSONObject()
        root.put("v", FORMAT_VERSION)
        root.put("sales", arr)
        return root.toString().toByteArray(Charsets.UTF_8)
    }

    private fun decode(text: String): List<Sale> {
        val root = JSONObject(text)
        require(root.getInt("v") == FORMAT_VERSION) { "versi format tidak dikenal" }
        val arr = root.getJSONArray("sales")
        val out = ArrayList<Sale>(arr.length())
        for (idx in 0 until arr.length()) {
            val o = arr.getJSONObject(idx)
            val itemsArr = o.getJSONArray("items")
            require(itemsArr.length() in 1..MAX_SALE_ITEMS) { "jumlah item tidak wajar" }
            val items = ArrayList<SaleItem>(itemsArr.length())
            for (j in 0 until itemsArr.length()) {
                val io = itemsArr.getJSONObject(j)
                val id = io.getString("id")
                val name = io.getString("n")
                val price = io.getLong("p")
                val qty = io.getInt("q")
                require(id.length in 1..64 && name.length in 1..200) { "teks item tidak wajar" }
                require(price in 0..MAX_PRICE && qty in 1..1000) { "angka item tidak wajar" }
                items.add(SaleItem(id, name, price, qty))
            }
            val total = o.getLong("total")
            require(total == items.sumOf { it.subtotal }) { "total tidak konsisten" }
            val method = when (o.getString("m")) {
                "qris" -> PaymentMethod.QRIS
                "cash" -> PaymentMethod.CASH
                else -> throw IllegalArgumentException("metode tidak dikenal")
            }
            val sid = o.getString("id")
            val t = o.getLong("t")
            require(sid.length in 1..64 && t > 0) { "id/waktu tidak wajar" }
            out.add(
                Sale(
                    id = sid,
                    items = items,
                    total = total,
                    createdAtMillis = t,
                    method = method,
                    cashReceived = if (o.has("cash")) o.getLong("cash") else null,
                    change = if (o.has("chg")) o.getLong("chg") else null,
                )
            )
        }
        return out
    }

    private companion object {
        const val FILE = "sales"
    }
}

/** Keranjang aktif (id + jumlah saja), tersimpan terenkripsi seperti riwayat omzet. */
class CartRepository(private val store: SecureStore) {

    suspend fun load(): LoadResult<List<CartLine>> = withContext(Dispatchers.IO) {
        try {
            val bytes = store.read(FILE) ?: return@withContext LoadResult(emptyList<CartLine>(), false)
            LoadResult(decode(String(bytes, Charsets.UTF_8)), false)
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            store.quarantine(FILE)
            LoadResult(emptyList<CartLine>(), true)
        }
    }

    suspend fun save(lines: List<CartLine>) = withContext(Dispatchers.IO) {
        val arr = JSONArray()
        for (l in lines) {
            val o = JSONObject()
            o.put("id", l.itemId)
            o.put("q", l.quantity)
            arr.put(o)
        }
        val root = JSONObject()
        root.put("v", FORMAT_VERSION)
        root.put("lines", arr)
        store.write(FILE, root.toString().toByteArray(Charsets.UTF_8))
    }

    /** Baris dengan id tidak dikenal dibuang; jumlah dipaksa ke 1..MAX. Harga tidak pernah disimpan. */
    private fun decode(text: String): List<CartLine> {
        val root = JSONObject(text)
        require(root.getInt("v") == FORMAT_VERSION) { "versi format tidak dikenal" }
        val arr = root.getJSONArray("lines")
        val out = ArrayList<CartLine>()
        val seen = HashSet<String>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.getString("id")
            if (Catalog.find(id) == null || !seen.add(id)) continue
            out.add(CartLine(id, o.getInt("q").coerceIn(1, Limits.MAX_QTY_PER_LINE)))
        }
        return out
    }

    private companion object {
        const val FILE = "cart"
    }
}
