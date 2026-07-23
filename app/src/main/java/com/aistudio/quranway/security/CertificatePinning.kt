package com.aistudio.quranway.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.security.MessageDigest

/**
 * Certificate Pinning configuration
 * Prevents MITM attacks by pinning to specific certificates
 */
class CertificatePinning {
    
    companion object {
        // SHA-256 hashes of public keys to pin
        // Production APIs
        private val publicKeysHashes = setOf(
            // Example: Your API server certificate hash
            "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
            "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
        )
        
        /**
         * Get certificate pins for OkHttp
         */
        fun getCertificatePins(): Map<String, Set<String>> {
            return mapOf(
                // Add your API domains and their certificate hashes
                "api.example.com" to publicKeysHashes,
                "cdn.example.com" to publicKeysHashes
            )
        }
    }
}
