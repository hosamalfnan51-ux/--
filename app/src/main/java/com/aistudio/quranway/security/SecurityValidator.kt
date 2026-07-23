package com.aistudio.quranway.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.security.MessageDigest
import kotlin.math.abs

/**
 * Root detection & Security checks
 * Prevents app from running on compromised devices
 */
class SecurityValidator(private val context: Context) {
    
    fun validateDeviceSecurity(): Boolean {
        return !isDeviceRooted() && !isEmulatorDetected() && !hasDebuggerAttached()
    }
    
    /**
     * Detect if device is rooted using multiple methods
     */
    private fun isDeviceRooted(): Boolean {
        return checkRootFiles() || checkSuBinary() || checkDangerousProperties()
    }
    
    private fun checkRootFiles(): Boolean {
        val suspiciousFiles = arrayOf(
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/data/data/com.noshufou.android.su",
            "/data/data/com.koushikdutta.superuser",
            "/data/data/com.zachspong.temprootremover",
            "/data/data/com.yellowes.su",
            "/data/app/com.noshufou.android.su*",
            "/data/app/com.koushikdutta.superuser*",
            "/system/xbin/daemonsu"
        )
        
        for (file in suspiciousFiles) {
            if (java.io.File(file).exists()) {
                Log.w("Security", "Suspicious file detected: $file")
                return true
            }
        }
        return false
    }
    
    private fun checkSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/bin/sudo",
            "/system/xbin/sudo"
        )
        
        for (path in paths) {
            if (java.io.File(path).exists()) {
                Log.w("Security", "Root binary detected: $path")
                return true
            }
        }
        return false
    }
    
    private fun checkDangerousProperties(): Boolean {
        val dangerousProps = mapOf(
            "ro.debuggable" to "1",
            "ro.secure" to "0",
            "persist.service.adb.enable" to "1",
            "init.svc.adbd" to "running"
        )
        
        return dangerousProps.any { (prop, value) ->
            try {
                val clazz = Class.forName("android.os.SystemProperties")
                val method = clazz.getMethod("get", String::class.java)
                val propValue = method.invoke(null, prop) as String
                propValue == value
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Detect if app is running on emulator
     */
    private fun isEmulatorDetected(): Boolean {
        return (
            Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.DEVICE.contains("emulator") ||
            Build.PRODUCT.contains("sdk") ||
            Settings.Secure.getString(context.contentResolver, "ro.kernel.android.checkjni") == "1"
        )
    }
    
    /**
     * Check if debugger is attached
     */
    private fun hasDebuggerAttached(): Boolean {
        return android.os.Debug.isDebuggerConnected() ||
               android.os.Debug.waitingForDebugger()
    }
    
    /**
     * Verify app integrity (Anti-tampering)
     */
    fun verifyAppIntegrity(): Boolean {
        return try {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
            info.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE == 0
        } catch (e: Exception) {
            false
        }
    }
}
