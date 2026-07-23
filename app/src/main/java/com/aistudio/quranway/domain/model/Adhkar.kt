package com.aistudio.quranway.domain.model

data class Dhikr(
    val id: Int,
    val textArabic: String,
    val textEnglish: String = "",
    val category: String,
    val repetitions: Int = 1,
    val source: String = "",
    val benefits: String = "",
    val audioUrl: String = ""
)

data class DailyAdhkar(
    val date: String,
    val morningDhikr: List<Dhikr>,
    val eveningDhikr: List<Dhikr>,
    val completedMorning: Boolean = false,
    val completedEvening: Boolean = false
)
