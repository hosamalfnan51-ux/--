package com.aistudio.quranway.domain.repository

import com.aistudio.quranway.domain.model.PrayerTime
import com.aistudio.quranway.domain.model.Qibla
import kotlinx.coroutines.flow.Flow

interface PrayerRepository {
    suspend fun getPrayerTimes(latitude: Double, longitude: Double, date: String): PrayerTime?
    suspend fun getPrayerTimesForMonth(latitude: Double, longitude: Double, month: String): List<PrayerTime>
    suspend fun getQiblaDirection(latitude: Double, longitude: Double): Qibla
    
    fun observePrayerTimes(): Flow<PrayerTime>
    suspend fun setUserLocation(latitude: Double, longitude: Double, cityName: String)
}
