package com.aistudio.quranway.domain.model

data class Surah(
    val id: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val versesCount: Int,
    val revelationPlace: String,
    val revelationOrder: Int,
    val description: String = ""
)

data class Verse(
    val id: Int,
    val surahId: Int,
    val verseNumber: Int,
    val textArabic: String,
    val textEnglish: String = "",
    val tafsir: String = "",
    val audioUrl: String = "",
    val tajweedRules: List<String> = emptyList()
)

data class VerseBookmark(
    val id: Int,
    val verseId: Int,
    val surahId: Int,
    val verseNumber: Int,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
