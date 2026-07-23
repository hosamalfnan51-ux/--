package com.example.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

data class MuezzinVoice(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val audioUrl: String,
    val location: String = "المملكة العربية السعودية"
)

object PrayerNotificationHelper {

    private const val CHANNEL_ID = "prayer_reminders_channel"
    private const val CHANNEL_NAME = "أوقات الصلاة والتذكيرات / Prayer Reminders"

    val muezzinVoices = listOf(
        MuezzinVoice("makkah", "الشيخ علي ملا (أذان الحرم المكي الشريف)", "Sheikh Ali Mulla (Makkah Adhan)", "https://download.quranicaudio.com/adhan/makkah.mp3", "مكة المكرمة"),
        MuezzinVoice("madinah", "الشيخ عبد المجيد السريحي (أذان المسجد النبوي)", "Sheikh Abdul Majid Surhi (Madinah Adhan)", "https://download.quranicaudio.com/adhan/madinah.mp3", "المدينة المنورة"),
        MuezzinVoice("alafasy", "الشيخ مشاري راشد العفاسي", "Sheikh Mishary Alafasy", "https://download.quranicaudio.com/adhan/alafasy.mp3", "دولة الكويت"),
        MuezzinVoice("abdulbasit", "الشيخ عبد الباسط عبد الصمد", "Sheikh Abdul Basit", "https://download.quranicaudio.com/adhan/abdulbasit.mp3", "جمهورية مصر العربية"),
        MuezzinVoice("mustafa_ismail", "الشيخ مصطفى إسماعيل", "Sheikh Mustafa Ismail", "https://download.quranicaudio.com/adhan/mustafa_ismail.mp3", "جمهورية مصر العربية"),
        MuezzinVoice("minshawi", "الشيخ محمد صديق المنشاوي", "Sheikh Siddiq Al-Minshawi", "https://download.quranicaudio.com/quran/muhammad_siddeeq_al-minshawi/001.mp3", "مصر - تجويد مبارك")
    )

    private var previewPlayer: MediaPlayer? = null

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات عند حان وقت الصلاة واقتراب موعدها / Notifications when prayer time arrives"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows notification 5 minutes before prayer time ("اقترب موعد الصلاة") with custom vibration & audio notification.
     */
    fun showPrePrayerWarningNotification(
        context: Context,
        prayerNameAr: String,
        prayerNameEn: String,
        isEnglish: Boolean
    ) {
        createNotificationChannel(context)

        val title = if (isEnglish) "Upcoming Prayer in 5 Minutes ⏳" else "اقترب موعد صلاة $prayerNameAr (باقي 5 دقائق) ⏳"
        val message = if (isEnglish) "Only 5 minutes remaining for $prayerNameEn prayer. Prepare for wudu and prayer."
        else "اقترب موعد صلاة $prayerNameAr، باقي 5 دقائق فقط. استعد للوضوء والصلاة بارك الله فيك."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 600, 200, 600))
            .setAutoCancel(true)

        triggerVibration(context)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify((System.currentTimeMillis() % 100000).toInt() + 555, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Shows notification when prayer time arrives with optional Adhan voice audio playback.
     */
    fun showPrayerNotification(
        context: Context,
        prayerNameAr: String,
        prayerNameEn: String,
        isEnglish: Boolean,
        audioUrl: String = ""
    ) {
        createNotificationChannel(context)

        val title = if (isEnglish) "Time for $prayerNameEn Prayer 🕌" else "حان الآن موعد صلاة $prayerNameAr 🕌"
        val message = if (isEnglish) "Perform your prayer on time and gain Allah's reward." else "حي على الصلاة.. حي على الفلاح! حافظ على صلاتك في أوقاتها."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 800, 300, 800, 300, 800))
            .setAutoCancel(true)

        triggerVibration(context)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())

            if (audioUrl.isNotBlank()) {
                playAudioFromUrl(context, audioUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerVibration(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 500, 200, 500), -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Play preview audio for Muezzin selection.
     */
    fun playPreviewAdhan(context: Context, audioUrl: String, onFinished: () -> Unit = {}) {
        stopPreviewAdhan()
        try {
            previewPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, Uri.parse(audioUrl))
                setOnPreparedListener { start() }
                setOnCompletionListener {
                    stopPreviewAdhan()
                    onFinished()
                }
                setOnErrorListener { _, _, _ ->
                    stopPreviewAdhan()
                    onFinished()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopPreviewAdhan()
            onFinished()
        }
    }

    fun stopPreviewAdhan() {
        try {
            previewPlayer?.stop()
            previewPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            previewPlayer = null
        }
    }

    private fun playAudioFromUrl(context: Context, url: String) {
        try {
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            mp.setDataSource(context, Uri.parse(url))
            mp.setOnPreparedListener { mp.start() }
            mp.setOnCompletionListener { mp.release() }
            mp.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

