package id.biz.majulancar.kasir.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object Limits {
    const val MAX_QTY_PER_LINE = 99
    const val MAX_CASH_DIGITS = 9
    const val MAX_CASH = 999_999_999L
    const val MAX_HISTORY_SHOWN = 100
}

val ShopZone: ZoneId = ZoneId.of("Asia/Jakarta")

data class MenuItem(val id: String, val name: String, val price: Long)

data class MenuCategory(
    val key: String,
    val label: String,
    val emoji: String,
    val items: List<MenuItem>,
)

/** Keranjang hanya menyimpan id + jumlah. Harga SELALU dibaca dari [Catalog]. */
data class CartLine(val itemId: String, val quantity: Int)

enum class PaymentMethod { QRIS, CASH }

data class SaleItem(val id: String, val name: String, val price: Long, val quantity: Int) {
    val subtotal: Long get() = price * quantity
}

data class Sale(
    val id: String,
    val items: List<SaleItem>,
    val total: Long,
    val createdAtMillis: Long,
    val method: PaymentMethod,
    val cashReceived: Long?,
    val change: Long?,
)

data class SalesSummary(
    val totalOmzet: Long,
    val totalTransaksi: Int,
    val omzetHariIni: Long,
    val transaksiHariIni: Int,
    val omzetQris: Long,
    val transaksiQris: Int,
    val omzetTunai: Long,
    val transaksiTunai: Int,
) {
    companion object {
        fun of(sales: List<Sale>, nowMillis: Long = System.currentTimeMillis()): SalesSummary {
            val today: LocalDate = Instant.ofEpochMilli(nowMillis).atZone(ShopZone).toLocalDate()
            val todays = sales.filter {
                Instant.ofEpochMilli(it.createdAtMillis).atZone(ShopZone).toLocalDate() == today
            }
            val qris = sales.filter { it.method == PaymentMethod.QRIS }
            val cash = sales.filter { it.method == PaymentMethod.CASH }
            return SalesSummary(
                totalOmzet = sales.sumOf { it.total },
                totalTransaksi = sales.size,
                omzetHariIni = todays.sumOf { it.total },
                transaksiHariIni = todays.size,
                omzetQris = qris.sumOf { it.total },
                transaksiQris = qris.size,
                omzetTunai = cash.sumOf { it.total },
                transaksiTunai = cash.size,
            )
        }
    }
}
