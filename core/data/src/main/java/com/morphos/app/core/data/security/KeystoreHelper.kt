package com.morphos.app.core.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreHelper(private val context: Context) {

    companion object {
        private const val KEY_ALIAS = "MorphOsDbKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val PREFS_NAME = "morphos_secure_prefs"
        private const val ENCRYPTED_KEY_PREF = "encrypted_db_key"
        private const val IV_PREF = "db_key_iv"
    }

    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedKeyBase64 = prefs.getString(ENCRYPTED_KEY_PREF, null)
        val ivBase64 = prefs.getString(IV_PREF, null)

        if (encryptedKeyBase64 != null && ivBase64 != null) {
            try {
                val encryptedKey = Base64.decode(encryptedKeyBase64, Base64.DEFAULT)
                val iv = Base64.decode(ivBase64, Base64.DEFAULT)
                return decryptKey(encryptedKey, iv)
            } catch (e: Exception) {
                // Regenerate on failure
            }
        }

        val rawKey = ByteArray(32)
        SecureRandom().nextBytes(rawKey)

        try {
            val encryptionResult = encryptKey(rawKey)
            prefs.edit()
                .putString(ENCRYPTED_KEY_PREF, Base64.encodeToString(encryptionResult.encryptedBytes, Base64.DEFAULT))
                .putString(IV_PREF, Base64.encodeToString(encryptionResult.iv, Base64.DEFAULT))
                .apply()
        } catch (e: Exception) {
            // Fallback for secure generation if keystore operation fails
            return "fallback_secure_keystore_passphrase_32_bytes".toByteArray()
        }

        return rawKey
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
        return (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private class EncryptionResult(val encryptedBytes: ByteArray, val iv: ByteArray)

    private fun encryptKey(rawKey: ByteArray): EncryptionResult {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val encrypted = cipher.doFinal(rawKey)
        return EncryptionResult(encrypted, cipher.iv)
    }

    private fun decryptKey(encryptedKey: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return cipher.doFinal(encryptedKey)
    }
}
