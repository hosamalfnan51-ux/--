package com.aistudio.quranway.domain.model

data class HifzPlan(
    val id: Int,
    val name: String,
    val startSurah: Int,
    val endSurah: Int,
    val dailyTarget: Int,
    val createdAt: Long,
    val progress: Int = 0
)

data class HifzSession(
    val id: Int,
    val planId: Int,
    val surahId: Int,
    val verseStart: Int,
    val verseEnd: Int,
    val date: String,
    val completed: Boolean = false,
    val accuracy: Float = 0f,
    val audioRecording: String = ""
)

data class RecitationEvaluation(
    val accuracy: Float,
    val tajweedErrors: List<String>,
    val strengths: List<String>,
    val suggestions: List<String>,
    val overallScore: Float
)
