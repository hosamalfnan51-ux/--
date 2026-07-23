package com.aistudio.quranway.domain.model

data class KhatmaRoom(
    val id: Int,
    val name: String,
    val description: String,
    val creatorId: String,
    val members: List<String>,
    val progress: Float = 0f,
    val createdAt: Long,
    val deadline: Long? = null,
    val isPublic: Boolean = true
)

data class KhatamaProgress(
    val roomId: Int,
    val userId: String,
    val surahsCompleted: List<Int>,
    val progress: Float,
    val lastUpdated: Long
)
