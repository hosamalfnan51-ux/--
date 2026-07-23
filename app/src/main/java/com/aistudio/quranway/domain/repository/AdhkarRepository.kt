package com.aistudio.quranway.domain.repository

import com.aistudio.quranway.domain.model.Dhikr
import com.aistudio.quranway.domain.model.DailyAdhkar
import kotlinx.coroutines.flow.Flow

interface AdhkarRepository {
    suspend fun getDailyAdhkar(date: String): DailyAdhkar
    suspend fun getAllDhikr(): List<Dhikr>
    suspend fun getDhikrByCategory(category: String): List<Dhikr>
    suspend fun searchDhikr(query: String): List<Dhikr>
    
    fun observeDailyAdhkarProgress(): Flow<DailyAdhkar>
    suspend fun markMorningAsComplete(date: String): Boolean
    suspend fun markEveningAsComplete(date: String): Boolean
}
