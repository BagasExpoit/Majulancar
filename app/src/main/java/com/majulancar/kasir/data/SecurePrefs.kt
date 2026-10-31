package com.majulancar.kasir.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Single source of on-device storage for the app: cart contents and the
 * local sales ledger (omzet). Both are kept in an
 * [EncryptedSharedPreferences] file backed by a hardware-backed Android
 * Keystore master key (AES256-GCM for values, AES256-SIV for keys), so the
 * data is unreadable if the device is rooted and the file is pulled, or if
 * the APK/device is backed up (backups are also disabled — see
 * AndroidManifest `allowBackup="false"` and `data_extraction_rules.xml`).
 */
object SecurePrefs {

    private const val FILE_NAME = "majulancar_secure_store"

    @Volatile
    private var instance: SharedPreferences? = null

    fun get(context: Context): SharedPreferences {
        instance?.let { return it }
        synchronized(this) {
            instance?.let { return it }
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context.applicationContext,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            instance = prefs
            return prefs
        }
    }
}
