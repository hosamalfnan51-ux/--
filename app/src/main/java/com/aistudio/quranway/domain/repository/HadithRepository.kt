package com.aistudio.quranway.domain.repository

import com.aistudio.quranway.domain.model.Hadith
import com.aistudio.quranway.domain.model.HadithCategory
import kotlinx.coroutines.flow.Flow

interface HadithRepository {
    suspend fun getAllHadiths(): List<Hadith>
    suspend fun getHadithsByCategory(category: String): List<Hadith>
    suspend fun getHadithCategories(): List<HadithCategory>
    suspend fun searchHadiths(query: String): List<Hadith>
    suspend fun getHadith(hadithId: Int): Hadith?
    
    fun getFavoriteHadiths(): Flow<List<Hadith>>
    suspend fun addToFavorites(hadithId: Int): Boolean
    suspend fun removeFromFavorites(hadithId: Int): Boolean
}
