package com.aistudio.quranway.domain.model

data class PrayerTime(
    val date: String,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val nextPrayerName: String,
    val nextPrayerTime: String,
    val remainingTime: String,
    val latitude: Double,
    val longitude: Double,
    val cityName: String
)

data class Qibla(
    val latitude: Double,
    val longitude: Double,
    val direction: Float,
    val accuracy: Float
)
