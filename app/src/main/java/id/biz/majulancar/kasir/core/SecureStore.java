package id.biz.majulancar.kasir.core;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.ProviderException;
import java.util.regex.Pattern;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Penyimpanan berkas terenkripsi di folder privat aplikasi.
 *
 *  - Kunci AES-256 dibuat & disimpan di Android Keystore (tidak pernah keluar dari
 *    perangkat/TEE; StrongBox dipakai bila tersedia).
 *  - Nama berkas dibatasi ^[a-z0-9_]{1,32}$ (anti path traversal).
 *  - Penulisan atomik (berkas sementara + rename) agar tidak rusak saat proses mati.
 *  - Ukuran berkas dibatasi untuk mencegah OOM dari berkas yang dimanipulasi.
 */
public final class SecureStore {

    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "majulancar_kasir_data_key_v1";
    private static final Pattern NAME = Pattern.compile("^[a-z0-9_]{1,32}$");
    private static final int MAX_BYTES = 8 * 1024 * 1024;

    private final File dir;

    public SecureStore(Context context) {
        this.dir = new File(context.getFilesDir(), "secure");
    }

    /** @return isi terdekripsi, atau null bila berkas belum ada. */
    public synchronized byte[] read(String name) throws IOException, GeneralSecurityException {
        File f = fileFor(name);
        if (!f.exists()) {
            return null;
        }
        if (f.length() > MAX_BYTES + 64) {
            throw new IOException("Berkas melebihi batas ukuran.");
        }
        byte[] blob = new byte[(int) f.length()];
        try (FileInputStream in = new FileInputStream(f)) {
            int off = 0;
            while (off < blob.length) {
                int r = in.read(blob, off, blob.length - off);
                if (r < 0) {
                    throw new IOException("Berkas terpotong.");
                }
                off += r;
            }
        }
        return AesGcm.decrypt(getOrCreateKey(), blob, aad(name));
    }

    public synchronized void write(String name, byte[] plain) throws IOException, GeneralSecurityException {
        if (plain.length > MAX_BYTES) {
            throw new IOException("Data melebihi batas ukuran penyimpanan.");
        }
        File target = fileFor(name);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            throw new IOException("Gagal membuat folder penyimpanan.");
        }
        byte[] blob = AesGcm.encrypt(getOrCreateKey(), plain, aad(name));
        File tmp = new File(parent, name + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            out.write(blob);
            out.getFD().sync();
        }
        Files.move(tmp.toPath(), target.toPath(),
                StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    public synchronized void delete(String name) throws IOException {
        File f = fileFor(name);
        if (f.exists() && !f.delete()) {
            throw new IOException("Gagal menghapus berkas.");
        }
    }

    /** Pindahkan berkas rusak ke *.corrupt (bukan dihapus) supaya masih bisa dipulihkan manual. */
    public synchronized void quarantine(String name) {
        File f = fileFor(name);
        if (f.exists()) {
            File dest = new File(f.getParentFile(), name + ".corrupt-" + System.currentTimeMillis());
            //noinspection ResultOfMethodCallIgnored
            f.renameTo(dest);
        }
    }

    private File fileFor(String name) {
        if (name == null || !NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("Nama berkas tidak valid.");
        }
        return new File(dir, name + ".bin");
    }

    private static byte[] aad(String name) {
        return ("majulancar:v1:" + name).getBytes(StandardCharsets.UTF_8);
    }

    private static SecretKey getOrCreateKey() throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEYSTORE);
        ks.load(null);
        KeyStore.Entry entry = ks.getEntry(KEY_ALIAS, null);
        if (entry instanceof KeyStore.SecretKeyEntry) {
            return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
        }
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                return generateKey(true);
            } catch (ProviderException strongBoxUnavailable) {
                // StrongBox tidak tersedia di perangkat ini -> pakai TEE biasa.
            }
        }
        return generateKey(false);
    }

    private static SecretKey generateKey(boolean strongBox) throws GeneralSecurityException {
        KeyGenParameterSpec.Builder b = new KeyGenParameterSpec.Builder(
                KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true);
        if (strongBox && Build.VERSION.SDK_INT >= 28) {
            b.setIsStrongBoxBacked(true);
        }
        KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        kg.init(b.build());
        return kg.generateKey();
    }
}
