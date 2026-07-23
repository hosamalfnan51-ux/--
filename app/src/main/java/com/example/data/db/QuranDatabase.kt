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
import com.example.data.model.CachedTafsirEntity
import com.example.data.model.CachedVerseEntity
import com.example.data.model.DailyDhikrBookmarkEntity
import com.example.data.model.HifzPlan
import com.example.data.model.HifzProgress
import com.example.data.model.KhatmaRoom
import com.example.data.model.JuzHifzEntity
import com.example.data.model.ReadingGoalEntity
import com.example.data.model.SurahHifzEntity
import com.example.data.model.VerseHifzEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahVerseHifzDao {
    @Query("SELECT * FROM juz_hifz_status ORDER BY juzNumber ASC")
    fun getAllJuzHifzStatus(): Flow<List<JuzHifzEntity>>

    @Query("SELECT * FROM juz_hifz_status WHERE juzNumber = :juzNumber LIMIT 1")
    suspend fun getJuzHifzStatus(juzNumber: Int): JuzHifzEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateJuzHifz(entity: JuzHifzEntity)

    @Query("SELECT * FROM surah_hifz_status ORDER BY surahId ASC")
    fun getAllSurahHifzStatus(): Flow<List<SurahHifzEntity>>

    @Query("SELECT * FROM surah_hifz_status WHERE surahId = :surahId LIMIT 1")
    suspend fun getSurahHifzStatus(surahId: Int): SurahHifzEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSurahHifz(entity: SurahHifzEntity)

    @Query("SELECT * FROM verse_hifz_status WHERE surahId = :surahId ORDER BY verseNumber ASC")
    fun getVerseHifzStatusForSurah(surahId: Int): Flow<List<VerseHifzEntity>>

    @Query("SELECT * FROM verse_hifz_status WHERE juzNumber = :juzNumber ORDER BY surahId ASC, verseNumber ASC")
    fun getVerseHifzStatusForJuz(juzNumber: Int): Flow<List<VerseHifzEntity>>

    @Query("SELECT * FROM verse_hifz_status WHERE verseKey = :verseKey LIMIT 1")
    suspend fun getVerseHifzStatus(verseKey: String): VerseHifzEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateVerseHifz(entity: VerseHifzEntity)
}

@Dao
interface QuranCacheDao {
    @Query("SELECT * FROM cached_verses WHERE surahId = :surahId ORDER BY verseNumber ASC")
    suspend fun getVersesForSurah(surahId: Int): List<CachedVerseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<CachedVerseEntity>)

    @Query("SELECT * FROM cached_tafsir WHERE id = :id LIMIT 1")
    suspend fun getCachedTafsir(id: String): CachedTafsirEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTafsir(tafsir: CachedTafsirEntity)
}

@Dao
interface ReadingPlannerDao {
    @Query("SELECT * FROM reading_goals WHERE id = 1 LIMIT 1")
    fun getReadingGoalFlow(): Flow<ReadingGoalEntity?>

    @Query("SELECT * FROM reading_goals WHERE id = 1 LIMIT 1")
    suspend fun getReadingGoal(): ReadingGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGoal(goal: ReadingGoalEntity)
}

@Dao
interface DailyDhikrDao {
    @Query("SELECT * FROM daily_dhikr_bookmarks")
    fun getAllBookmarks(): Flow<List<DailyDhikrBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: DailyDhikrBookmarkEntity)

    @Query("DELETE FROM daily_dhikr_bookmarks WHERE dhikrId = :dhikrId")
    suspend fun removeBookmark(dhikrId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM daily_dhikr_bookmarks WHERE dhikrId = :dhikrId)")
    suspend fun isBookmarked(dhikrId: Int): Boolean
}

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

@Database(
    entities = [
        HifzPlan::class,
        HifzProgress::class,
        KhatmaRoom::class,
        CachedVerseEntity::class,
        CachedTafsirEntity::class,
        ReadingGoalEntity::class,
        DailyDhikrBookmarkEntity::class,
        JuzHifzEntity::class,
        SurahHifzEntity::class,
        VerseHifzEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun hifzDao(): HifzDao
    abstract fun surahVerseHifzDao(): SurahVerseHifzDao
    abstract fun khatmaDao(): KhatmaDao
    abstract fun quranCacheDao(): QuranCacheDao
    abstract fun readingPlannerDao(): ReadingPlannerDao
    abstract fun dailyDhikrDao(): DailyDhikrDao

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
