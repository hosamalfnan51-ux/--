package com.aistudio.quranway.domain.repository

import com.aistudio.quranway.domain.model.Dua
import com.aistudio.quranway.domain.model.DuaCategory
import kotlinx.coroutines.flow.Flow

interface DuaRepository {
    suspend fun getAllDuas(): List<Dua>
    suspend fun getDuasByCategory(category: String): List<Dua>
    suspend fun getDuaCategories(): List<DuaCategory>
    suspend fun searchDuas(query: String): List<Dua>
    suspend fun getDua(duaId: Int): Dua?
    
    fun getFavoriteDuas(): Flow<List<Dua>>
    suspend fun addToFavorites(duaId: Int): Boolean
    suspend fun removeFromFavorites(duaId: Int): Boolean
}
