package com.example.data.repository

import java.util.Calendar
import java.util.Locale
import kotlin.math.*

data class PrayerTimeItem(
    val nameArabic: String,
    val nameEnglish: String,
    val timeFormatted: String, // e.g., "04:30 AM"
    val timestamp: Long,
    val isNext: Boolean = false,
    val isPassed: Boolean = false
)

data class PrayerTimesData(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val list: List<PrayerTimeItem>,
    val nextPrayerName: String,
    val nextPrayerFormatted: String,
    val remainingTimeFormatted: String,
    val qiblaAngle: Float, // Direction in degrees relative to True North
    val distanceToKaabaKm: Int
)

data class CityPreset(
    val nameArabic: String,
    val nameEnglish: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneOffsetHours: Double
)

object PrayerTimesManager {

    val defaultCities = listOf(
        CityPreset("مكة المكرمة", "Makkah", 21.4225, 39.8262, 3.0),
        CityPreset("المدينة المنورة", "Madinah", 24.4672, 39.6112, 3.0),
        CityPreset("القاهرة", "Cairo", 30.0444, 31.2357, 3.0),
        CityPreset("الرياض", "Riyadh", 24.7136, 46.6753, 3.0),
        CityPreset("دبي", "Dubai", 25.2048, 55.2708, 4.0),
        CityPreset("إسطنبول", "Istanbul", 41.0082, 28.9784, 3.0),
        CityPreset("لندن", "London", 51.5074, -0.1278, 1.0),
        CityPreset("نيويورك", "New York", 40.7128, -74.0060, -4.0),
        CityPreset("جاكرتا", "Jakarta", -6.2088, 106.8456, 7.0),
        CityPreset("كوالالمبور", "Kuala Lumpur", 3.1390, 101.6869, 8.0)
    )

    // Kaaba Coordinates
    const val KAABA_LAT = 21.4225
    const val KAABA_LNG = 39.8262

    /**
     * Calculates Qibla direction in degrees clockwise from True North.
     */
    fun calculateQiblaDirection(lat: Double, lng: Double): Float {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLambda = Math.toRadians(KAABA_LNG - lng)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)

        var qibla = Math.toDegrees(atan2(y, x)).toFloat()
        if (qibla < 0) {
            qibla += 360f
        }
        return qibla
    }

    /**
     * Calculates geodesic distance to Kaaba in Kilometers.
     */
    fun calculateDistanceToKaabaKm(lat: Double, lng: Double): Int {
        val r = 6371.0 // Earth radius in KM
        val dLat = Math.toRadians(KAABA_LAT - lat)
        val dLng = Math.toRadians(KAABA_LNG - lng)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat)) * cos(Math.toRadians(KAABA_LAT)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toInt()
    }

    /**
     * Calculates daily prayer times using standard solar elevation formulas.
     */
    fun calculatePrayerTimes(
        lat: Double,
        lng: Double,
        cityName: String = "الموقع الحالي",
        calendar: Calendar = Calendar.getInstance()
    ): PrayerTimesData {
        val tzOffset = calendar.timeZone.getOffset(calendar.timeInMillis) / 3600000.0
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // Solar Declination & Equation of Time approximations
        val b = 2 * Math.PI * (dayOfYear - 81) / 365.0
        val eqt = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b) // Equation of time in minutes
        val decl = 23.45 * sin(Math.PI / 180 * (360.0 / 365.0 * (dayOfYear - 81))) // Solar declination in degrees

        // Solar Noon in local time hours
        val noon = 12.0 + tzOffset - (lng / 15.0) - (eqt / 60.0)

        // Helper function for Sun Hour Angle given zenith angle
        fun hourAngle(angle: Double): Double {
            val cosH = (cos(Math.toRadians(angle)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(decl))) /
                    (cos(Math.toRadians(lat)) * cos(Math.toRadians(decl)))
            val clamped = cosH.coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(clamped)) / 15.0
        }

        // Zenith angles: Fajr = 18°, Sunrise/Sunset = 90.833°, Isha = 17.5°
        val fajrH = hourAngle(108.0) // 90 + 18
        val sunriseH = hourAngle(90.833)
        val ishaH = hourAngle(107.5) // 90 + 17.5

        // Asr Calculation (Shafi'i: shadow length = 1 + solar noon shadow)
        val asrAngle = 90.0 - Math.toDegrees(atan(1.0 + tan(Math.toRadians(abs(lat - decl)))))
        val asrH = hourAngle(90.0 - asrAngle)

        val fajrTime = noon - fajrH
        val sunriseTime = noon - sunriseH
        val dhuhrTime = noon + 0.05 // + 3 mins safety
        val asrTime = noon + asrH
        val maghribTime = noon + sunriseH + 0.05
        val ishaTime = noon + ishaH

        fun formatHours(hours: Double): String {
            var h = hours
            while (h < 0) h += 24.0
            while (h >= 24) h -= 24.0
            val totalMinutes = (h * 60).toInt()
            val m = totalMinutes % 60
            val hr24 = totalMinutes / 60
            val ampm = if (hr24 >= 12) "PM" else "AM"
            val hr12 = if (hr24 % 12 == 0) 12 else hr24 % 12
            return String.format(Locale.ENGLISH, "%02d:%02d %s", hr12, m, ampm)
        }

        fun getTimeInMillis(hours: Double): Long {
            var h = hours
            while (h < 0) h += 24.0
            while (h >= 24) h -= 24.0
            val cal = calendar.clone() as Calendar
            val totalMinutes = (h * 60).toInt()
            cal.set(Calendar.HOUR_OF_DAY, totalMinutes / 60)
            cal.set(Calendar.MINUTE, totalMinutes % 60)
            cal.set(Calendar.SECOND, 0)
            return cal.timeInMillis
        }

        val fStr = formatHours(fajrTime)
        val srStr = formatHours(sunriseTime)
        val dhStr = formatHours(dhuhrTime)
        val asrStr = formatHours(asrTime)
        val magStr = formatHours(maghribTime)
        val ishaStr = formatHours(ishaTime)

        val nowMs = calendar.timeInMillis

        val rawList = listOf(
            Triple("الفجر", "Fajr", fajrTime),
            Triple("الشروق", "Sunrise", sunriseTime),
            Triple("الظهر", "Dhuhr", dhuhrTime),
            Triple("العصر", "Asr", asrTime),
            Triple("المغرب", "Maghrib", maghribTime),
            Triple("العشاء", "Isha", ishaTime)
        )

        var nextFound = false
        var nextNameAr = "الفجر"
        var nextNameEn = "Fajr"
        var nextFormatted = fStr
        var remainingMs = 0L

        val items = rawList.map { (ar, en, hrs) ->
            val ts = getTimeInMillis(hrs)
            val passed = nowMs > ts
            var isN = false
            if (!passed && !nextFound) {
                isN = true
                nextFound = true
                nextNameAr = ar
                nextNameEn = en
                nextFormatted = formatHours(hrs)
                remainingMs = ts - nowMs
            }
            PrayerTimeItem(
                nameArabic = ar,
                nameEnglish = en,
                timeFormatted = formatHours(hrs),
                timestamp = ts,
                isNext = isN,
                isPassed = passed
            )
        }

        if (!nextFound) {
            // All passed today, next is Fajr tomorrow
            val tomorrowFajrMs = getTimeInMillis(fajrTime) + 24 * 60 * 60 * 1000
            remainingMs = tomorrowFajrMs - nowMs
            nextNameAr = "الفجر"
            nextNameEn = "Fajr"
            nextFormatted = fStr
        }

        val remHours = (remainingMs / (1000 * 60 * 60)).toInt()
        val remMins = ((remainingMs / (1000 * 60)) % 60).toInt()
        val remSecs = ((remainingMs / 1000) % 60).toInt()
        val remainingFormatted = String.format(Locale.ENGLISH, "%02d:%02d:%02d", remHours, remMins, remSecs)

        val qibla = calculateQiblaDirection(lat, lng)
        val distanceKaaba = calculateDistanceToKaabaKm(lat, lng)

        return PrayerTimesData(
            cityName = cityName,
            latitude = lat,
            longitude = lng,
            fajr = fStr,
            sunrise = srStr,
            dhuhr = dhStr,
            asr = asrStr,
            maghrib = magStr,
            isha = ishaStr,
            list = items,
            nextPrayerName = nextNameAr,
            nextPrayerFormatted = nextFormatted,
            remainingTimeFormatted = remainingFormatted,
            qiblaAngle = qibla,
            distanceToKaabaKm = distanceKaaba
        )
    }
}
