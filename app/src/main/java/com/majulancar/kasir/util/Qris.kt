package com.majulancar.kasir.util

/**
 * QRIS with an embedded amount (port of the web app's lib/qris.ts).
 *
 * Turns the merchant's STATIC QRIS payload into a DYNAMIC one carrying the
 * bill amount, so the customer just scans and pays without typing a
 * number. Per EMVCo, inserting the amount (tag 54) requires switching the
 * Point-of-Initiation indicator (tag 01) from "11" (static) to "12"
 * (dynamic), and the CRC16 (tag 63) must be recomputed afterwards.
 */
object Qris {

    private const val TAG_POI_STATIC = "010211"
    private const val TAG_POI_DYNAMIC = "010212"
    private const val COUNTRY_CODE_TAG = "5802ID"

    /** Length of the trailing CRC tag: "63" + "04" + 4 hex chars. */
    private const val CRC_TAG_LENGTH = 8

    data class Result(
        val payload: String,
        val isDynamic: Boolean,
        val error: String? = null
    )

    /** CRC16-CCITT (poly 0x1021, init 0xFFFF) as 4 uppercase hex digits. */
    fun crc16ccitt(payload: String): String {
        var crc = 0xFFFF
        for (ch in payload) {
            crc = crc xor (ch.code shl 8)
            for (bit in 0 until 8) {
                crc = if ((crc and 0x8000) != 0) {
                    ((crc shl 1) xor 0x1021) and 0xFFFF
                } else {
                    (crc shl 1) and 0xFFFF
                }
            }
        }
        return String.format("%04X", crc)
    }

    /** Returns null when [payload] looks like a sane QRIS payload, otherwise the reason. */
    fun validate(payload: String): String? {
        if (payload.length < 20) return "Payload QRIS terlalu pendek atau kosong."
        if (!payload.startsWith("000201")) return "Payload tidak diawali indikator versi EMV (000201)."
        if (!payload.contains(COUNTRY_CODE_TAG)) return "Tag Country Code (58=ID) tidak ditemukan pada payload."
        return null
    }

    private fun tlv(tag: String, value: String): String =
        tag + value.length.toString().padStart(2, '0') + value

    /** @throws IllegalArgumentException if the payload or amount is invalid. */
    fun generateDynamic(staticPayload: String, amount: Long): String {
        val problem = validate(staticPayload)
        require(problem == null) { "Payload QRIS tidak valid: $problem" }
        require(amount > 0) { "Nominal transaksi harus lebih besar dari 0." }

        // 1. Drop the old CRC tag completely (8 chars), not just its value.
        var payload = staticPayload.dropLast(CRC_TAG_LENGTH)

        // 2. Static (11) -> dynamic (12), first occurrence only.
        payload = payload.replaceFirst(TAG_POI_STATIC, TAG_POI_DYNAMIC)

        // 3. Insert tag 54 (amount) right before tag 58 (country code).
        val idx = payload.indexOf(COUNTRY_CODE_TAG)
        require(idx >= 0) { "Tag Country Code (58) tidak ditemukan, gagal menyisipkan nominal." }
        payload = payload.substring(0, idx) + tlv("54", amount.toString()) + payload.substring(idx)

        // 4. Recompute CRC over everything + "6304".
        val withCrcHeader = payload + "6304"
        return withCrcHeader + crc16ccitt(withCrcHeader)
    }

    /** Never throws: on failure falls back to the static payload (customer types the amount). */
    fun generateDynamicSafe(staticPayload: String, amount: Long): Result =
        try {
            Result(generateDynamic(staticPayload, amount), isDynamic = true)
        } catch (e: IllegalArgumentException) {
            Result(staticPayload, isDynamic = false, error = e.message)
        }
}
