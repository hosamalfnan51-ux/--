package com.aistudio.quranway.domain.model

data class ReadingGoal(
    val id: Int,
    val name: String,
    val startDate: String,
    val endDate: String,
    val startSurah: Int,
    val endSurah: Int,
    val dailyTarget: Int,
    val progress: Int = 0,
    val completed: Boolean = false
)

data class DailyReadingProgress(
    val date: String,
    val goalId: Int,
    val versesRead: Int,
    val completedToday: Boolean = false,
    val timeSpent: Long = 0
)
