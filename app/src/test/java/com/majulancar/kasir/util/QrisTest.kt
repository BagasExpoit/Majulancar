package com.majulancar.kasir.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QrisTest {

    // Real static payload of the store (assets/menu.json); its own CRC is 69EE.
    private val staticPayload =
        "00020101021126570011ID.DANA.WWW011893600915345573715302094557371530303UMI" +
            "51440014ID.CO.QRIS.WWW0215ID10232667059130303UMI5204581253033605802ID" +
            "5925MIE AYAM MAJU LANCAR MAS 6010Kab. Bogor610516680630469EE"

    @Test
    fun crc_standardCheckValue() {
        assertEquals("29B1", Qris.crc16ccitt("123456789"))
    }

    @Test
    fun crc_matchesTheStaticPayloadOfTheStore() {
        assertEquals("69EE", Qris.crc16ccitt(staticPayload.dropLast(4)))
    }

    @Test
    fun dynamicPayload_hasAmountDynamicFlagAndValidCrc() {
        val p = Qris.generateDynamic(staticPayload, 12_000)
        assertTrue(p.contains("010212"))
        assertFalse(p.contains("010211"))
        assertTrue(p.contains("540512000" + "5802ID"))
        assertEquals(Qris.crc16ccitt(p.dropLast(4)), p.takeLast(4))
        assertEquals(1, Regex("6304").findAll(p.takeLast(8)).count())
    }

    @Test
    fun invalidInput_fallsBackToStaticPayload() {
        val r = Qris.generateDynamicSafe(staticPayload, 0)
        assertFalse(r.isDynamic)
        assertEquals(staticPayload, r.payload)
    }
}
