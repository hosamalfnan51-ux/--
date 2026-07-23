package com.aistudio.quranway.domain.repository

import com.aistudio.quranway.domain.model.Tafsir
import com.aistudio.quranway.domain.model.TafsirTheme

interface TafsirRepository {
    suspend fun getTafsir(surahId: Int, verseNumber: Int): Tafsir?
    suspend fun getTafsirsForSurah(surahId: Int): List<Tafsir>
    suspend fun getTafsirThemes(): List<TafsirTheme>
    suspend fun getTafsirsByTheme(theme: String): List<Tafsir>
    suspend fun getAITafsir(verseText: String): String
    suspend fun getVoiceTafsir(surahId: Int, verseNumber: Int): String
}
