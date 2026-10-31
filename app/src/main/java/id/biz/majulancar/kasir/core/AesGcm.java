package id.biz.majulancar.kasir.core;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Enkripsi AES-256-GCM murni (tanpa API Android) agar bisa diuji di JVM.
 *
 * Format blob: [versi:1][panjangIV:1][IV][ciphertext+tag]
 * AAD (nama berkas) ikut diautentikasi, sehingga blob dari satu berkas tidak bisa
 * ditukar ke berkas lain tanpa terdeteksi.
 */
public final class AesGcm {

    private static final byte VERSION = 1;
    private static final int TAG_BITS = 128;
    private static final int MIN_IV = 12;
    private static final int MAX_IV = 16;

    private AesGcm() {
    }

    public static byte[] encrypt(SecretKey key, byte[] plain, byte[] aad) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key); // IV acak dibuat provider (Keystore mewajibkannya)
        byte[] iv = cipher.getIV();
        if (iv == null || iv.length < MIN_IV || iv.length > MAX_IV) {
            throw new GeneralSecurityException("IV tidak valid.");
        }
        cipher.updateAAD(aad);
        byte[] ct = cipher.doFinal(plain);

        byte[] out = new byte[2 + iv.length + ct.length];
        out[0] = VERSION;
        out[1] = (byte) iv.length;
        System.arraycopy(iv, 0, out, 2, iv.length);
        System.arraycopy(ct, 0, out, 2 + iv.length, ct.length);
        return out;
    }

    public static byte[] decrypt(SecretKey key, byte[] blob, byte[] aad) throws GeneralSecurityException {
        if (blob == null || blob.length < 2 + MIN_IV + 16) {
            throw new GeneralSecurityException("Blob terenkripsi terlalu pendek.");
        }
        if (blob[0] != VERSION) {
            throw new GeneralSecurityException("Versi format tidak dikenal.");
        }
        int ivLen = blob[1] & 0xFF;
        if (ivLen < MIN_IV || ivLen > MAX_IV || blob.length < 2 + ivLen + 16) {
            throw new GeneralSecurityException("Panjang IV tidak valid.");
        }
        byte[] iv = Arrays.copyOfRange(blob, 2, 2 + ivLen);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        cipher.updateAAD(aad);
        return cipher.doFinal(blob, 2 + ivLen, blob.length - 2 - ivLen);
    }
}
