package id.biz.majulancar.kasir.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.biz.majulancar.kasir.core.SecureStore
import id.biz.majulancar.kasir.data.Catalog
import id.biz.majulancar.kasir.data.CartLine
import id.biz.majulancar.kasir.data.CartRepository
import id.biz.majulancar.kasir.data.Limits
import id.biz.majulancar.kasir.data.MenuItem
import id.biz.majulancar.kasir.data.PaymentMethod
import id.biz.majulancar.kasir.data.Sale
import id.biz.majulancar.kasir.data.SaleItem
import id.biz.majulancar.kasir.data.SalesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigInteger
import java.security.SecureRandom

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SecureStore(app)
    private val salesRepo = SalesRepository(store)
    private val cartRepo = CartRepository(store)
    private val random = SecureRandom()
    private val prefs = app.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)
    private val writeMutex = Mutex()

    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart: StateFlow<List<CartLine>> = _cart.asStateFlow()

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()

    private val _pending = MutableStateFlow<Map<String, Int>>(emptyMap())
    /** Jumlah yang sedang dipilih di halaman Menu sebelum ditekan "Tambah" (default 1). */
    val pending: StateFlow<Map<String, Int>> = _pending.asStateFlow()

    private val _themeOverride = MutableStateFlow<Boolean?>(
        when (prefs.getString("theme", null)) {
            "dark" -> true
            "light" -> false
            else -> null
        }
    )
    /** true = gelap, false = terang, null = ikuti pengaturan sistem (belum pernah dipilih manual). */
    val themeOverride: StateFlow<Boolean?> = _themeOverride.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    // Penjaga integritas: bila riwayat gagal dimuat, DILARANG menulis (agar tidak menimpa
    // data lama dengan daftar kosong).
    private var salesReady = false
    private var cartReady = false

    init {
        viewModelScope.launch {
            try {
                val s = salesRepo.load()
                _sales.value = s.value
                salesReady = true
                if (s.quarantined) {
                    postMessage("Data omzet lama tidak bisa dibaca dan telah diamankan. Riwayat dimulai dari awal.")
                }
            } catch (e: Exception) {
                postMessage("Gagal memuat riwayat omzet. Transaksi baru dinonaktifkan; tutup lalu buka ulang aplikasi.")
            }
            try {
                val c = cartRepo.load()
                _cart.value = c.value
                cartReady = true
            } catch (e: Exception) {
                postMessage("Gagal memuat keranjang tersimpan.")
            }
            _loading.value = false
        }
    }

    // ---- Tema ----

    fun setDarkTheme(dark: Boolean) {
        _themeOverride.value = dark
        prefs.edit().putString("theme", if (dark) "dark" else "light").apply()
    }

    // ---- Menu ----

    fun changePending(itemId: String, delta: Int) {
        _pending.update { m ->
            val cur = m[itemId] ?: 1
            m + (itemId to (cur + delta).coerceIn(1, Limits.MAX_QTY_PER_LINE))
        }
    }

    fun addToCart(item: MenuItem) {
        if (Catalog.find(item.id) == null) return
        val qty = _pending.value[item.id] ?: 1
        _cart.update { lines ->
            val existing = lines.firstOrNull { it.itemId == item.id }
            if (existing == null) {
                lines + CartLine(item.id, qty.coerceIn(1, Limits.MAX_QTY_PER_LINE))
            } else {
                lines.map {
                    if (it.itemId == item.id) {
                        it.copy(quantity = (it.quantity + qty).coerceIn(1, Limits.MAX_QTY_PER_LINE))
                    } else {
                        it
                    }
                }
            }
        }
        _pending.update { it + (item.id to 1) }
        persistCart()
        postMessage("${qty}x ${item.name} ditambahkan")
    }

    // ---- Keranjang ----

    fun changeQty(itemId: String, delta: Int) {
        _cart.update { lines ->
            lines.map {
                if (it.itemId == itemId) {
                    it.copy(quantity = (it.quantity + delta).coerceIn(1, Limits.MAX_QTY_PER_LINE))
                } else {
                    it
                }
            }
        }
        persistCart()
    }

    fun removeLine(itemId: String) {
        _cart.update { lines -> lines.filter { it.itemId != itemId } }
        persistCart()
        postMessage("Item dihapus")
    }

    fun clearCart() {
        _cart.value = emptyList()
        persistCart()
        postMessage("Keranjang dikosongkan")
    }

    /**
     * Selesaikan pembayaran. Harga & total dihitung ulang dari [Catalog] (bukan dari UI).
     * Jika penyimpanan gagal, keranjang DIPERTAHANKAN dan [onResult] menerima false.
     */
    fun checkout(method: PaymentMethod, cashReceived: Long?, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val lines = _cart.value
            val items = lines.mapNotNull { l ->
                Catalog.find(l.itemId)?.let { SaleItem(it.id, it.name, it.price, l.quantity) }
            }
            if (items.isEmpty() || items.size != lines.size) {
                postMessage("Keranjang tidak valid.")
                onResult(false)
                return@launch
            }
            val total: Long = items.sumOf { it.subtotal }
            var cash: Long? = null
            var change: Long? = null
            if (method == PaymentMethod.CASH) {
                if (cashReceived == null || cashReceived < total || cashReceived > Limits.MAX_CASH) {
                    postMessage("Uang diterima belum cukup untuk melunasi total belanja.")
                    onResult(false)
                    return@launch
                }
                cash = cashReceived
                change = cashReceived - total
            }
            val sale = Sale(
                id = newSaleId(),
                items = items,
                total = total,
                createdAtMillis = System.currentTimeMillis(),
                method = method,
                cashReceived = cash,
                change = change,
            )
            val ok = writeMutex.withLock {
                if (!salesReady) {
                    false
                } else {
                    try {
                        val next = _sales.value + sale
                        salesRepo.save(next)
                        _sales.value = next
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            if (ok) {
                _cart.value = emptyList()
                persistCart()
                postMessage(
                    if (method == PaymentMethod.CASH) {
                        "Pembayaran tunai berhasil! Kembalian ${(change ?: 0L).rupiah()}"
                    } else {
                        "Pembayaran QRIS tercatat. Terima kasih!"
                    }
                )
            } else {
                postMessage("Gagal menyimpan transaksi. Keranjang dipertahankan, coba lagi.")
            }
            onResult(ok)
        }
    }

    // ---- Omzet ----

    fun resetSales() {
        viewModelScope.launch {
            val ok = writeMutex.withLock {
                if (!salesReady) {
                    false
                } else {
                    try {
                        salesRepo.save(emptyList())
                        _sales.value = emptyList()
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
            }
            postMessage(if (ok) "Riwayat omzet berhasil direset" else "Gagal mereset riwayat omzet")
        }
    }

    // ---- internal ----

    private fun persistCart() {
        viewModelScope.launch {
            writeMutex.withLock {
                if (cartReady) {
                    try {
                        cartRepo.save(_cart.value)
                    } catch (e: Exception) {
                        postMessage("Keranjang tidak dapat disimpan ke penyimpanan aman.")
                    }
                }
            }
        }
    }

    private fun newSaleId(): String {
        val suffix = BigInteger(30, random).toString(36).uppercase().padStart(6, '0')
        return "TRX-${System.currentTimeMillis()}-$suffix"
    }

    private fun postMessage(message: String) {
        _messages.trySend(message)
    }
}
