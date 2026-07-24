package com.example.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class ReadingGoalReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastInteractionTime = prefs.getLong(KEY_LAST_INTERACTION_TIME, 0L)
        val isEnglish = prefs.getBoolean(KEY_IS_ENGLISH, false)
        val now = System.currentTimeMillis()

        // Check if user has not interacted for 24 hours (24 * 60 * 60 * 1000L)
        val twentyFourHoursMs = 24 * 60 * 60 * 1000L
        if (lastInteractionTime == 0L || (now - lastInteractionTime) >= twentyFourHoursMs) {
            showReadingGoalReminderNotification(context, isEnglish)
        }

        return Result.success()
    }

    companion object {
        const val PREFS_NAME = "quran_app_prefs"
        const val KEY_LAST_INTERACTION_TIME = "last_app_interaction_time"
        const val KEY_IS_ENGLISH = "is_english_language"

        private const val CHANNEL_ID = "reading_goal_reminders_channel"
        private const val CHANNEL_NAME = "تذكيرات الورد اليومي / Reading Goal Reminders"
        private const val WORK_TAG = "READING_GOAL_REMINDER_WORK"

        fun updateLastInteractionTime(context: Context, isEnglish: Boolean = false) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putLong(KEY_LAST_INTERACTION_TIME, System.currentTimeMillis())
                .putBoolean(KEY_IS_ENGLISH, isEnglish)
                .apply()
        }

        fun scheduleReadingGoalReminder(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<ReadingGoalReminderWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(24, TimeUnit.HOURS)
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun showReadingGoalReminderNotification(context: Context, isEnglish: Boolean) {
            createNotificationChannel(context)

            val title = if (isEnglish) "📖 Daily Quran Reading Reminder" else "📖 تذكير بوردك اليومي من القرآن الكريم"
            val message = if (isEnglish) {
                "You haven't read your daily Quran portion in 24 hours. Open the app to complete your goal!"
            } else {
                "لم تقم بتلاوة وردك اليومي منذ 24 ساعة. افتح المصحف للوصول إلى هدفك القرآني بارك الله فيك."
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_today)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(0, 500, 250, 500))
                .setAutoCancel(true)

            try {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(1002, builder.build())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تنبيهات تذكرك بالورد اليومي وقراءة القرآن عند عدم التفاعل لممدة 24 ساعة"
                    enableVibration(true)
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }
    }
}
