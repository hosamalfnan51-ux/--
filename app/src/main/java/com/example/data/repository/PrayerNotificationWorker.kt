package com.example.data.repository

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class PrayerNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val prayerNameAr = inputData.getString("PRAYER_NAME_AR") ?: "الصلاة"
        val prayerNameEn = inputData.getString("PRAYER_NAME_EN") ?: "Prayer"
        val isPreReminder = inputData.getBoolean("IS_PRE_REMINDER", false)
        val isEnglish = inputData.getBoolean("IS_ENGLISH", false)
        val audioUrl = inputData.getString("AUDIO_URL") ?: ""

        if (isPreReminder) {
            PrayerNotificationHelper.showPrePrayerWarningNotification(context, prayerNameAr, prayerNameEn, isEnglish)
        } else {
            PrayerNotificationHelper.showPrayerNotification(context, prayerNameAr, prayerNameEn, isEnglish, audioUrl)
        }

        return Result.success()
    }

    companion object {
        fun schedulePrayerWorker(
            context: Context,
            prayerNameAr: String,
            prayerNameEn: String,
            delayMs: Long,
            isPreReminder: Boolean,
            isEnglish: Boolean,
            audioUrl: String
        ) {
            if (delayMs <= 0) return

            val data = workDataOf(
                "PRAYER_NAME_AR" to prayerNameAr,
                "PRAYER_NAME_EN" to prayerNameEn,
                "IS_PRE_REMINDER" to isPreReminder,
                "IS_ENGLISH" to isEnglish,
                "AUDIO_URL" to audioUrl
            )

            val tag = if (isPreReminder) "PRE_$prayerNameEn" else "EXACT_$prayerNameEn"

            val workRequest = OneTimeWorkRequestBuilder<PrayerNotificationWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(tag)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                tag,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
