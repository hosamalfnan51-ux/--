package com.aistudio.quranway.domain.model

data class Hadith(
    val id: Int,
    val textArabic: String,
    val textEnglish: String = "",
    val source: String,
    val narrator: String,
    val gradeOfAuthenticity: String,
    val explanation: String = "",
    val audioUrl: String = ""
)

data class HadithCategory(
    val id: Int,
    val name: String,
    val description: String,
    val hadithCount: Int
)
