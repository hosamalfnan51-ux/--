package com.aistudio.quranway.domain.repository

import com.aistudio.quranway.domain.model.ReadingGoal
import com.aistudio.quranway.domain.model.DailyReadingProgress
import kotlinx.coroutines.flow.Flow

interface ReadingGoalRepository {
    suspend fun createReadingGoal(goal: ReadingGoal): Int
    suspend fun getReadingGoals(): List<ReadingGoal>
    suspend fun getActiveReadingGoal(): ReadingGoal?
    suspend fun updateReadingProgress(goalId: Int, versesRead: Int)
    
    fun observeReadingProgress(): Flow<DailyReadingProgress>
    suspend fun completeReadingForDay(goalId: Int, date: String): Boolean
    suspend fun getReadingStats(): Map<String, Any>
}
