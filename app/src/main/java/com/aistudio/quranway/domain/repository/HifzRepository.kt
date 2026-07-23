package com.aistudio.quranway.domain.repository

import com.aistudio.quranway.domain.model.HifzPlan
import com.aistudio.quranway.domain.model.HifzSession
import com.aistudio.quranway.domain.model.RecitationEvaluation
import kotlinx.coroutines.flow.Flow

interface HifzRepository {
    suspend fun createHifzPlan(plan: HifzPlan): Int
    suspend fun getHifzPlans(): List<HifzPlan>
    suspend fun getHifzPlan(planId: Int): HifzPlan?
    suspend fun updateHifzProgress(planId: Int, progress: Int)
    
    suspend fun createHifzSession(session: HifzSession): Int
    suspend fun getSessionsForPlan(planId: Int): List<HifzSession>
    suspend fun evaluateRecitation(audioPath: String): RecitationEvaluation
    
    fun observeHifzProgress(): Flow<HifzPlan>
}
