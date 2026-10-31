package com.majulancar.kasir.data

/**
 * Data models for the kiosk app. These intentionally mirror the shapes
 * used by the original web app's `data/menu.json` and sales API so the
 * same source-of-truth data file can be reused as-is in `assets/menu.json`.
 */

data class StoreHours(
    val label: String,
    val open: String,
    val close: String
)

data class StoreRating(
    val value: Double,
    val reviewCount: Int,
    val source: String
)

data class StoreLocation(
    val lat: Double,
    val lng: Double,
    val zoom: Int,
    val mapsUrl: String
)

data class Store(
    val name: String,
    val address: String,
    val adminContactUrl: String,
    val phone: String,
    val whatsappUrl: String,
    val hours: StoreHours,
    val rating: StoreRating,
    val qrisPayload: String,
    val location: StoreLocation
)

data class MenuItem(
    val id: String,
    val name: String,
    val price: Long
)

data class MenuCategory(
    val key: String,
    val label: String,
    val photo: String?,
    val photoCredit: String?,
    val items: List<MenuItem>
)

data class MenuData(
    val store: Store,
    val categories: List<MenuCategory>
)

/** A line item sitting in the on-device cart. */
data class CartItem(
    val id: String,
    val name: String,
    val price: Long,
    var quantity: Int
) {
    val subtotal: Long get() = price * quantity
}

enum class PaymentMethod { QRIS, CASH }

/** A completed, locally-recorded sale (replaces the old server-side /api/sales). */
data class Sale(
    val id: String,
    val items: List<CartItem>,
    val total: Long,
    val createdAtEpochMillis: Long,
    val paymentMethod: PaymentMethod,
    val cashReceived: Long? = null,
    val change: Long? = null
)

data class SalesSummary(
    val totalOmzet: Long,
    val totalTransaksi: Int,
    val omzetHariIni: Long,
    val transaksiHariIni: Int,
    val omzetQris: Long,
    val omzetTunai: Long,
    val transaksiQris: Int,
    val transaksiTunai: Int,
    val riwayat: List<Sale>
)
