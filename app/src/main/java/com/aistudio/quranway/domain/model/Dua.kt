package com.aistudio.quranway.domain.model

data class Dua(
    val id: Int,
    val titleArabic: String,
    val titleEnglish: String = "",
    val textArabic: String,
    val textEnglish: String = "",
    val category: String,
    val occasion: String = "",
    val source: String,
    val audioUrl: String = "",
    val benefits: String = ""
)

data class DuaCategory(
    val id: Int,
    val name: String,
    val description: String,
    val icon: String
)
