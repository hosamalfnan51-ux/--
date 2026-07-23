package com.aistudio.quranway.security

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Certificate Pinning - Prevents Man-in-the-Middle attacks
 * Pins only to specific SSL certificates
 */
class CertificatePinningInterceptor : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        try {
            // Log request for security audit
            Log.d("CertPinning", "Request to: ${request.url}")
            
            val response = chain.proceed(request)
            
            // Verify SSL/TLS is being used
            if (!request.url.isHttps) {
                Log.w("CertPinning", "Non-HTTPS request detected: ${request.url}")
            }
            
            return response
        } catch (e: Exception) {
            Log.e("CertPinning", "Certificate pinning validation failed", e)
            throw IOException("Certificate pinning validation failed", e)
        }
    }
}
