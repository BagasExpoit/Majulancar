package id.biz.majulancar.kasir.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.Test;

public class AesGcmTest {

    private static SecretKey newKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        return kg.generateKey();
    }

    private static final byte[] AAD = "majulancar:v1:sales".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PLAIN = "{\"v\":1,\"sales\":[]}".getBytes(StandardCharsets.UTF_8);

    @Test
    public void roundTrip() throws Exception {
        SecretKey k = newKey();
        assertArrayEquals(PLAIN, AesGcm.decrypt(k, AesGcm.encrypt(k, PLAIN, AAD), AAD));
    }

    @Test
    public void ciphertextHidesPlaintextAndUsesFreshIv() throws Exception {
        SecretKey k = newKey();
        byte[] a = AesGcm.encrypt(k, PLAIN, AAD);
        byte[] b = AesGcm.encrypt(k, PLAIN, AAD);
        assertFalse(new String(a, StandardCharsets.ISO_8859_1).contains("sales"));
        assertFalse(Arrays.equals(a, b));
    }

    @Test
    public void tamperingIsDetected() throws Exception {
        SecretKey k = newKey();
        byte[] blob = AesGcm.encrypt(k, PLAIN, AAD);
        blob[blob.length - 1] ^= 1;
        try {
            AesGcm.decrypt(k, blob, AAD);
            fail("harus gagal");
        } catch (GeneralSecurityException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void blobCannotBeSwappedToAnotherFile() throws Exception {
        SecretKey k = newKey();
        byte[] blob = AesGcm.encrypt(k, PLAIN, AAD);
        try {
            AesGcm.decrypt(k, blob, "majulancar:v1:cart".getBytes(StandardCharsets.UTF_8));
            fail("harus gagal");
        } catch (GeneralSecurityException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void wrongKeyAndShortBlobAreRejected() throws Exception {
        byte[] blob = AesGcm.encrypt(newKey(), PLAIN, AAD);
        try {
            AesGcm.decrypt(newKey(), blob, AAD);
            fail("kunci salah harus gagal");
        } catch (GeneralSecurityException expected) {
            assertTrue(true);
        }
        try {
            AesGcm.decrypt(newKey(), new byte[5], AAD);
            fail("blob pendek harus gagal");
        } catch (GeneralSecurityException expected) {
            assertTrue(true);
        }
    }
}
