package com.aistudio.quranway.domain.repository

import com.aistudio.quranway.domain.model.KhatmaRoom
import com.aistudio.quranway.domain.model.KhatamaProgress
import kotlinx.coroutines.flow.Flow

interface KhatmaRepository {
    suspend fun createKhatmaRoom(room: KhatmaRoom): Int
    suspend fun getKhatmaRooms(): List<KhatmaRoom>
    suspend fun getKhatmaRoom(roomId: Int): KhatmaRoom?
    suspend fun joinKhatmaRoom(roomId: Int, userId: String): Boolean
    suspend fun leaveKhatmaRoom(roomId: Int, userId: String): Boolean
    
    suspend fun updateKhatmaProgress(roomId: Int, userId: String, progress: KhatamaProgress)
    fun observeKhatmaProgress(roomId: Int): Flow<List<KhatamaProgress>>
    suspend fun deleteKhatmaRoom(roomId: Int): Boolean
}
