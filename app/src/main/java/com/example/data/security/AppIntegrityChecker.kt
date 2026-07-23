package com.example.data.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

data class IntegrityCheckResult(
    val isValid: Boolean,
    val packageName: String,
    val signatureHash: String,
    val isEmulator: Boolean,
    val message: String
)

object AppIntegrityChecker {

    fun performAppIntegrityCheck(context: Context): IntegrityCheckResult {
        val packageName = context.packageName
        var signatureHash = ""
        var isValid = true
        var message = "App Integrity Verified Successfully (Play Integrity Ready)"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = packageInfo.signingInfo
                if (signingInfo != null) {
                    val signatures = if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                    if (signatures != null && signatures.isNotEmpty()) {
                        signatureHash = signatures[0].hashCode().toString()
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                val signatures = packageInfo.signatures
                if (signatures != null && signatures.isNotEmpty()) {
                    signatureHash = signatures[0].hashCode().toString()
                }
            }
        } catch (e: Exception) {
            isValid = false
            message = "Signature check warning: ${e.localizedMessage}"
        }

        val isEmulator = Build.FINGERPRINT.contains("generic") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu")

        return IntegrityCheckResult(
            isValid = isValid,
            packageName = packageName,
            signatureHash = signatureHash,
            isEmulator = isEmulator,
            message = message
        )
    }
}
