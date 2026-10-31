package com.majulancar.kasir.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Date
import java.util.Locale

/** Formats amounts and timestamps the same way the original site did (`id-ID` locale). */
object Formatter {

    private val idLocale = Locale("id", "ID")

    private val rupiahSymbols = DecimalFormatSymbols(idLocale).apply {
        groupingSeparator = '.'
    }
    private val rupiahFormat = DecimalFormat("#,###", rupiahSymbols)

    /** e.g. 12000 -> "Rp 12.000" */
    fun rupiah(amount: Long): String = "Rp ${rupiahFormat.format(amount)}"

    /** e.g. 12000 -> "12.000" (no "Rp" prefix, for compact labels) */
    fun number(amount: Long): String = rupiahFormat.format(amount)

    /** e.g. "30 Agustus 2026" */
    fun tanggal(epochMillis: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", idLocale)
        return sdf.format(Date(epochMillis))
    }

    /** e.g. "14:05:37" */
    fun jam(epochMillis: Long): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", idLocale)
        return sdf.format(Date(epochMillis))
    }

    /** e.g. "30 Agustus 2026 • 14:05:37 WIB" */
    fun waktu(epochMillis: Long): String = "${tanggal(epochMillis)} • ${jam(epochMillis)} WIB"
}
