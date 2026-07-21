package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.HifzPlan
import com.example.data.model.HifzProgress
import com.example.data.model.KhatmaRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface HifzDao {
    @Query("SELECT * FROM hifz_plans ORDER BY id DESC")
    fun getAllPlans(): Flow<List<HifzPlan>>

    @Query("SELECT * FROM hifz_plans WHERE id = :planId")
    suspend fun getPlanById(planId: Int): HifzPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: HifzPlan): Long

    @Update
    suspend fun updatePlan(plan: HifzPlan)

    @Query("DELETE FROM hifz_plans WHERE id = :planId")
    suspend fun deletePlan(planId: Int)

    @Query("SELECT * FROM hifz_progress WHERE planId = :planId ORDER BY timestamp ASC")
    fun getProgressForPlan(planId: Int): Flow<List<HifzProgress>>

    @Query("SELECT * FROM hifz_progress ORDER BY timestamp DESC")
    fun getAllProgress(): Flow<List<HifzProgress>>

    @Query("SELECT * FROM hifz_progress WHERE planId = :planId AND surahId = :surahId AND ayahId = :ayahId LIMIT 1")
    suspend fun getProgressByAyah(planId: Int, surahId: Int, ayahId: Int): HifzProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: HifzProgress): Long

    @Query("DELETE FROM hifz_progress WHERE planId = :planId AND surahId = :surahId AND ayahId = :ayahId")
    suspend fun deleteProgress(planId: Int, surahId: Int, ayahId: Int)
}

@Dao
interface KhatmaDao {
    @Query("SELECT * FROM khatma_rooms ORDER BY timestamp DESC")
    fun getAllKhatmas(): Flow<List<KhatmaRoom>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKhatma(khatma: KhatmaRoom)

    @Update
    suspend fun updateKhatma(khatma: KhatmaRoom)

    @Query("DELETE FROM khatma_rooms WHERE id = :id")
    suspend fun deleteKhatma(id: String)
}

@Database(entities = [HifzPlan::class, HifzProgress::class, KhatmaRoom::class], version = 1, exportSchema = false)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun hifzDao(): HifzDao
    abstract fun khatmaDao(): KhatmaDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getDatabase(context: Context): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "quranway_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
