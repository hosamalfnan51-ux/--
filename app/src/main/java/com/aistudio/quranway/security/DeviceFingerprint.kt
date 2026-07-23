package com.aistudio.quranway.security

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import java.util.UUID

/**
 * Secure device fingerprinting
 * Detects if app is being run on a different device
 */
class DeviceFingerprint(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "device_fingerprint",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val FINGERPRINT_KEY = "device_fingerprint"
        private const val DEVICE_ID_KEY = "device_id"
    }
    
    private val currentFingerprint: String
        get() = "${Build.DEVICE}_${Build.MODEL}_${Build.ANDROID_VERSION}" + 
                "_${Build.MANUFACTURER}_${Build.PRODUCT}"
    
    fun generateDeviceId() {
        if (!prefs.contains(DEVICE_ID_KEY)) {
            val deviceId = UUID.randomUUID().toString()
            prefs.edit().putString(DEVICE_ID_KEY, deviceId).apply()
        }
    }
    
    fun getDeviceId(): String {
        generateDeviceId()
        return prefs.getString(DEVICE_ID_KEY, "") ?: ""
    }
    
    /**
     * Check if device fingerprint matches
     * Returns false if app is running on different device
     */
    fun verifyDeviceFingerprint(): Boolean {
        val stored = prefs.getString(FINGERPRINT_KEY, null)
        val current = currentFingerprint
        
        if (stored == null) {
            prefs.edit().putString(FINGERPRINT_KEY, current).apply()
            return true
        }
        
        return stored == current
    }
    
    fun resetFingerprint() {
        prefs.edit().remove(FINGERPRINT_KEY).apply()
    }
}
