package com.aistudio.quranway.domain.model

data class Tafsir(
    val id: Int,
    val verseId: Int,
    val surahId: Int,
    val verseNumber: Int,
    val shortTafsir: String,
    val fullTafsir: String,
    val author: String,
    val audioUrl: String = "",
    val source: String
)

data class TafsirTheme(
    val id: Int,
    val name: String,
    val description: String,
    val verseIds: List<Int>
)
