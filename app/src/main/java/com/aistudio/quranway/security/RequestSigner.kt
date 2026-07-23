package com.aistudio.quranway.security

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * API request signing with HMAC-SHA256
 * Ensures data integrity and authenticity
 */
class RequestSigner(private val apiSecret: String) {
    
    companion object {
        private const val HMAC_ALGORITHM = "HmacSHA256"
    }
    
    /**
     * Sign request with HMAC-SHA256
     */
    fun signRequest(payload: String): String {
        return try {
            val secretKey = SecretKeySpec(
                apiSecret.toByteArray(Charsets.UTF_8),
                0,
                apiSecret.toByteArray(Charsets.UTF_8).size,
                HMAC_ALGORITHM
            )
            
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(secretKey)
            
            val digest = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(digest, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw SecurityException("Failed to sign request: ${e.message}")
        }
    }
    
    /**
     * Verify request signature
     */
    fun verifySignature(payload: String, signature: String): Boolean {
        return try {
            val expectedSignature = signRequest(payload)
            // Constant-time comparison to prevent timing attacks
            constantTimeEquals(expectedSignature, signature)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Constant-time string comparison
     * Prevents timing attacks
     */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        val aBytes = a.toByteArray()
        val bBytes = b.toByteArray()
        
        if (aBytes.size != bBytes.size) return false
        
        var result = 0
        for (i in aBytes.indices) {
            result = result or (aBytes[i].toInt() xor bBytes[i].toInt())
        }
        
        return result == 0
    }
}
