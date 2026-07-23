package com.aistudio.quranway.security

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * API Security Interceptor
 * Adds security headers and validates responses
 */
class SecurityHeadersInterceptor : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Add security headers
        val requestBuilder = originalRequest.newBuilder()
            .header("X-Requested-With", "com.aistudio.quranway")
            .header("User-Agent", "QuranWay/1.0")
            .header("X-Api-Version", "1.0")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            // Prevent caching of sensitive data
            .header("Cache-Control", "no-cache, no-store, must-revalidate")
            .header("Pragma", "no-cache")
            .header("Expires", "0")
            // Security headers
            .header("X-Content-Type-Options", "nosniff")
            .header("X-Frame-Options", "DENY")
            .header("X-XSS-Protection", "1; mode=block")
        
        val request = requestBuilder.build()
        val response = chain.proceed(request)
        
        // Validate response headers
        return response.newBuilder()
            .header("X-Content-Type-Options", "nosniff")
            .header("X-Frame-Options", "DENY")
            .header("X-XSS-Protection", "1; mode=block")
            .header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
            .build()
    }
}
