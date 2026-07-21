package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hifz_plans")
data class HifzPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val surahId: Int,
    val surahName: String,
    val startAyah: Int,
    val endAyah: Int,
    val startDate: Long,
    val targetCompletionDate: Long,
    val dailyAyahsCount: Int,
    val currentProgressAyah: Int = startAyah - 1,
    val isCompleted: Boolean = false
)

@Entity(tableName = "hifz_progress")
data class HifzProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val planId: Int,
    val surahId: Int,
    val ayahId: Int,
    val timestamp: Long,
    val status: Int, // 0 = Memorized, 1 = Under Review, 2 = Mastered
    val nextReviewDate: Long = System.currentTimeMillis(),
    val intervalDays: Int = 1,
    val repetitions: Int = 0,
    val easeFactor: Float = 2.5f
)

@Entity(tableName = "khatma_rooms")
data class KhatmaRoom(
    @PrimaryKey val id: String,
    val title: String,
    val creatorName: String,
    val targetDays: Int,
    val participantCount: Int,
    val claimedJuzListJson: String, // Map of JuzId (1-30) to Participant Name
    val progressPercentage: Float,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class Surah(
    val id: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val revelationPlace: String,
    val versesCount: Int,
    val startPage: Int
)

data class Verse(
    val id: Int,
    val verseNumber: Int,
    val textUthmani: String,
    val textIndopak: String = "",
    val translation: String = "",
    val audioUrl: String = ""
)
