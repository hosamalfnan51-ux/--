package com.aistudio.quranway.di

import android.content.Context
import com.aistudio.quranway.security.CertificatePinningInterceptor
import com.aistudio.quranway.security.DeviceFingerprint
import com.aistudio.quranway.security.EncryptionManager
import com.aistudio.quranway.security.RequestSigner
import com.aistudio.quranway.security.SecurePreferences
import com.aistudio.quranway.security.SecurityHeadersInterceptor
import com.aistudio.quranway.security.SecurityValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Security Module - Hilt DI for all security components
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideEncryptionManager(
        @ApplicationContext context: Context
    ): EncryptionManager {
        return EncryptionManager(context)
    }
    
    @Provides
    @Singleton
    fun provideSecurePreferences(
        @ApplicationContext context: Context
    ): SecurePreferences {
        return SecurePreferences(context)
    }
    
    @Provides
    @Singleton
    fun provideSecurityValidator(
        @ApplicationContext context: Context
    ): SecurityValidator {
        return SecurityValidator(context)
    }
    
    @Provides
    @Singleton
    fun provideDeviceFingerprint(
        @ApplicationContext context: Context
    ): DeviceFingerprint {
        return DeviceFingerprint(context)
    }
    
    @Provides
    @Singleton
    fun provideRequestSigner(): RequestSigner {
        return RequestSigner("your_api_secret_key_here")
    }
    
    @Provides
    @Singleton
    fun provideSecureOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(SecurityHeadersInterceptor())
            .addInterceptor(CertificatePinningInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // Enable TLS 1.2 and higher
            .retryOnConnectionFailure(true)
            .build()
    }
}
