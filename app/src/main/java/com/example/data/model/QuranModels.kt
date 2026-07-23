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

@Entity(tableName = "cached_verses")
data class CachedVerseEntity(
    @PrimaryKey(autoGenerate = true) val cacheId: Int = 0,
    val surahId: Int,
    val verseNumber: Int,
    val textUthmani: String,
    val textIndopak: String = "",
    val translation: String = "",
    val audioUrl: String = "",
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_tafsir")
data class CachedTafsirEntity(
    @PrimaryKey val id: String, // e.g., "surah_1_verse_1_en" or "surah_1_verse_1_ar"
    val surahId: Int,
    val verseNumber: Int,
    val surahName: String,
    val verseText: String,
    val tafsirText: String,
    val isEnglish: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_goals")
data class ReadingGoalEntity(
    @PrimaryKey val id: Int = 1,
    val targetDays: Int,
    val startDate: Long,
    val targetCompletionDate: Long,
    val pagesCompleted: Int = 0,
    val currentStreakDays: Int = 0,
    val lastReadDateTimestamp: Long = 0,
    val isCompleted: Boolean = false
)

@Entity(tableName = "daily_dhikr_bookmarks")
data class DailyDhikrBookmarkEntity(
    @PrimaryKey val dhikrId: Int,
    val dateBookmarked: Long = System.currentTimeMillis()
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
