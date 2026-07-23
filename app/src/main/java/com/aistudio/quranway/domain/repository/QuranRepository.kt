package com.aistudio.quranway.domain.repository

import com.aistudio.quranway.domain.model.Surah
import com.aistudio.quranway.domain.model.Verse
import com.aistudio.quranway.domain.model.VerseBookmark
import kotlinx.coroutines.flow.Flow

interface QuranRepository {
    suspend fun getAllSurahs(): List<Surah>
    suspend fun getSurah(surahId: Int): Surah?
    suspend fun getVerses(surahId: Int): List<Verse>
    suspend fun getVerse(surahId: Int, verseNumber: Int): Verse?
    suspend fun searchVerses(query: String): List<Verse>
    suspend fun getVersesByTheme(theme: String): List<Verse>
    
    fun getBookmarks(): Flow<List<VerseBookmark>>
    suspend fun addBookmark(bookmark: VerseBookmark): Boolean
    suspend fun removeBookmark(bookmarkId: Int): Boolean
    suspend fun updateBookmarkNote(bookmarkId: Int, note: String): Boolean
}
