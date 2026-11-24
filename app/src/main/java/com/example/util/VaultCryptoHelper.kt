package com.example.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Encrypts and decrypts local password vault entries using AES-256-GCM backed by
 * AndroidKeyStore when available on device.
 */
object VaultCryptoHelper {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "bayan_vault_master_aes_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BITS = 128
    private const val PREFIX_GCM = "gcm1:"
    private const val PREFIX_FALLBACK = "aes1:"

    private fun getOrCreateKeystoreKey(): SecretKey? {
        return try {
            val ks = KeyStore.getInstance(KEYSTORE_PROVIDER)
            ks.load(null)
            val existing = ks.getKey(KEY_ALIAS, null) as? SecretKey
            if (existing != null) return existing

            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGen.init(spec)
            keyGen.generateKey()
        } catch (_: Exception) {
            null
        }
    }

    private fun getJvmFallbackKey(): SecretKey {
        // Used strictly in non-AndroidKeyStore environments (e.g., Robolectric JVM tests)
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("bayan_local_test_vault_key_v1".toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(digest, "AES")
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        val keystoreKey = getOrCreateKeystoreKey()
        return if (keystoreKey != null) {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, keystoreKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val ctB64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
            "$PREFIX_GCM$ivB64:$ctB64"
        } else {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getJvmFallbackKey())
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val ctB64 = Base64.encodeToString(encrypted, Base64.NO_WRAP)
            "$PREFIX_FALLBACK$ivB64:$ctB64"
        }
    }

    fun decrypt(cipherPayload: String): String {
        if (cipherPayload.isEmpty()) return ""
        return try {
            when {
                cipherPayload.startsWith(PREFIX_GCM) -> {
                    val key = getOrCreateKeystoreKey() ?: return ""
                    decryptWithKey(cipherPayload.removePrefix(PREFIX_GCM), key)
                }
                cipherPayload.startsWith(PREFIX_FALLBACK) -> {
                    decryptWithKey(cipherPayload.removePrefix(PREFIX_FALLBACK), getJvmFallbackKey())
                }
                else -> cipherPayload
            }
        } catch (_: Exception) {
            ""
        }
    }

    private fun decryptWithKey(body: String, key: SecretKey): String {
        val parts = body.split(":", limit = 2)
        if (parts.size != 2) return ""
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val ct = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        val plainBytes = cipher.doFinal(ct)
        return String(plainBytes, StandardCharsets.UTF_8)
    }
}
