package com.aistudio.quranway.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.security.KeyStore
import java.security.SecureRandom

/**
 * Military-grade encryption for sensitive data
 * Uses Android Keystore + AES-GCM (256-bit) - NSA Suite B standard
 */
class EncryptionManager(private val context: Context) {
    
    companion object {
        private const val KEY_ALIAS = "quranway_secure_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$ENCRYPTION_PADDING"
        private const val GCM_TAG_LENGTH = 128 // bits
    }
    
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }
    
    init {
        createSecureKey()
    }
    
    /**
     * Create AES-GCM 256-bit key in Android Keystore
     * Key cannot be extracted - stays in secure hardware
     */
    private fun createSecureKey() {
        if (keyStore.containsAlias(KEY_ALIAS)) return
        
        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setKeySize(256) // 256-bit encryption
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(ENCRYPTION_PADDING)
            .setUserAuthenticationRequired(false)
            .setStrongBoxBacked(true) // Use Secure Hardware if available
            .build()
        
        KeyGenerator.getInstance(ALGORITHM, ANDROID_KEYSTORE).apply {
            init(keyGenSpec)
            generateKey()
        }
    }
    
    /**
     * Encrypt sensitive data with AES-GCM
     * Returns: Base64(IV + Ciphertext)
     */
    fun encrypt(plaintext: String): String {
        try {
            val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val iv = cipher.iv // 12 bytes for GCM
            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            
            // Combine IV + Ciphertext for transmission
            val combined = ByteArray(iv.size + ciphertext.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)
            
            return Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw SecurityException("Encryption failed: ${e.message}")
        }
    }
    
    /**
     * Decrypt AES-GCM encrypted data
     */
    fun decrypt(encryptedData: String): String {
        try {
            val key = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)
            
            // Extract IV (first 12 bytes)
            val iv = combined.copyOfRange(0, 12)
            val ciphertext = combined.copyOfRange(12, combined.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            
            val plaintext = cipher.doFinal(ciphertext)
            return String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            throw SecurityException("Decryption failed: ${e.message}")
        }
    }
}
