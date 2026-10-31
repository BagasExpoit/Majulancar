package id.biz.majulancar.kasir.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QrisTest {

    /** Payload QRIS statis toko (sama dengan StoreInfo.QRIS_STATIC_PAYLOAD). */
    private static final String STATIC_PAYLOAD =
            "00020101021126570011ID.DANA.WWW011893600915345573715302094557371530303UMI51440014ID.CO.QRIS.WWW0215ID10232667059130303UMI5204581253033605802ID5925MIE AYAM MAJU LANCAR MAS 6010Kab. Bogor610516680630469EE";

    // Vektor golden dihasilkan oleh lib/qris.ts (implementasi web asli) untuk payload di atas.
    private static final String GOLDEN_3000 =
            "00020101021226570011ID.DANA.WWW011893600915345573715302094557371530303UMI51440014ID.CO.QRIS.WWW0215ID10232667059130303UMI520458125303360540430005802ID5925MIE AYAM MAJU LANCAR MAS 6010Kab. Bogor6105166806304DDBD";
    private static final String GOLDEN_27000 =
            "00020101021226570011ID.DANA.WWW011893600915345573715302094557371530303UMI51440014ID.CO.QRIS.WWW0215ID10232667059130303UMI5204581253033605405270005802ID5925MIE AYAM MAJU LANCAR MAS 6010Kab. Bogor6105166806304384E";

    @Test
    public void staticPayloadIsValid() {
        assertNull(Qris.validate(STATIC_PAYLOAD));
        assertEquals("69EE", Qris.crc16(STATIC_PAYLOAD.substring(0, STATIC_PAYLOAD.length() - 4)));
    }

    @Test
    public void matchesOriginalWebImplementation() {
        assertEquals(GOLDEN_3000, Qris.generateDynamic(STATIC_PAYLOAD, 3000));
        assertEquals(GOLDEN_27000, Qris.generateDynamic(STATIC_PAYLOAD, 27000));
    }

    @Test
    public void dynamicPayloadIsItselfValid() {
        String dyn = Qris.generateDynamic(STATIC_PAYLOAD, 125000);
        assertNull(Qris.validate(dyn));
        assertTrue(dyn.contains("010212"));
        assertTrue(dyn.contains("5406125000" + "5802ID"));
    }

    @Test
    public void invalidAmountFallsBackToStatic() {
        assertFalse(Qris.generateDynamicSafe(STATIC_PAYLOAD, 0).isDynamic);
        assertFalse(Qris.generateDynamicSafe(STATIC_PAYLOAD, -1).isDynamic);
        assertFalse(Qris.generateDynamicSafe(STATIC_PAYLOAD, Qris.MAX_AMOUNT + 1).isDynamic);
        assertEquals(STATIC_PAYLOAD, Qris.generateDynamicSafe(STATIC_PAYLOAD, 0).payload);
    }

    @Test
    public void tamperedPayloadIsRejectedAndNeverModified() {
        char c = STATIC_PAYLOAD.charAt(30);
        String tampered = STATIC_PAYLOAD.substring(0, 30) + (c == '1' ? '2' : '1') + STATIC_PAYLOAD.substring(31);
        assertNotNull(Qris.validate(tampered));
        Qris.Result r = Qris.generateDynamicSafe(tampered, 1000);
        assertFalse(r.isDynamic);
        assertEquals(tampered, r.payload);
        assertNotNull(r.error);
    }

    @Test
    public void garbageAndNonAsciiAreRejected() {
        assertNotNull(Qris.validate(null));
        assertNotNull(Qris.validate(""));
        assertNotNull(Qris.validate("bukan payload qris sama sekali"));
        assertNotNull(Qris.validate(STATIC_PAYLOAD + "\u00e9"));
    }
}
