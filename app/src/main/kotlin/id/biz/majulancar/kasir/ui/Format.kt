package id.biz.majulancar.kasir.ui

import id.biz.majulancar.kasir.data.ShopZone
import id.biz.majulancar.kasir.data.StoreInfo
import java.text.NumberFormat
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val IndonesianLocale: Locale = Locale.Builder().setLanguage("id").setRegion("ID").build()

/** 27000 -> "Rp 27.000" */
fun Long.rupiah(): String = "Rp " + NumberFormat.getIntegerInstance(IndonesianLocale).format(this)

/** "31 Oktober 2026 • 14:05:37 WIB" */
fun formatWaktu(millis: Long): String {
    val f = DateTimeFormatter.ofPattern("dd MMMM yyyy • HH:mm:ss", IndonesianLocale)
    return f.format(Instant.ofEpochMilli(millis).atZone(ShopZone)) + " WIB"
}

fun isOpenNow(): Boolean {
    val now = ZonedDateTime.now(ShopZone)
    val minutes = now.hour * 60 + now.minute
    return minutes >= StoreInfo.OPEN_MINUTES && minutes < StoreInfo.CLOSE_MINUTES
}

/** Nominal uang tunai yang masuk akal berdasarkan total (pecahan umum di Indonesia). */
fun quickCashAmounts(total: Long): List<Long> {
    if (total <= 0) return emptyList()
    val suggestions = linkedSetOf(total)
    listOf(5_000L, 10_000L, 20_000L, 50_000L, 100_000L).forEach { if (it >= total) suggestions.add(it) }
    listOf(10_000L, 20_000L, 50_000L, 100_000L).forEach { base ->
        val rounded = ((total + base - 1) / base) * base
        if (rounded > total) suggestions.add(rounded)
    }
    return suggestions.sorted().take(5)
}
