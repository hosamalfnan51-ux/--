package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class QuranTranslation(
    @Json(name = "id") val id: Int?,
    @Json(name = "text") val text: String?
)

@JsonClass(generateAdapter = true)
data class QuranVerse(
    @Json(name = "id") val id: Int,
    @Json(name = "verse_number") val verseNumber: Int,
    @Json(name = "text_uthmani") val textUthmani: String?,
    @Json(name = "translations") val translations: List<QuranTranslation>?
)

@JsonClass(generateAdapter = true)
data class QuranVersesResponse(
    @Json(name = "verses") val verses: List<QuranVerse>
)

@JsonClass(generateAdapter = true)
data class QuranChapter(
    @Json(name = "id") val id: Int,
    @Json(name = "name_arabic") val nameArabic: String,
    @Json(name = "name_complex") val nameComplex: String,
    @Json(name = "revelation_place") val revelationPlace: String,
    @Json(name = "verses_count") val versesCount: Int,
    @Json(name = "pages") val pages: List<Int>?
)

@JsonClass(generateAdapter = true)
data class QuranChaptersResponse(
    @Json(name = "chapters") val chapters: List<QuranChapter>
)

interface QuranApiService {
    @GET("api/v4/chapters")
    suspend fun getChapters(
        @Query("language") language: String = "ar"
    ): QuranChaptersResponse

    @GET("api/v4/verses/by_chapter/{chapter_number}")
    suspend fun getVersesByChapter(
        @Path("chapter_number") chapterNumber: Int,
        @Query("language") language: String = "ar",
        @Query("words") words: Boolean = false,
        @Query("translations") translationIds: String = "131", // 131 is Sahih International english
        @Query("fields") fields: String = "text_uthmani"
    ): QuranVersesResponse
}

object QuranRetrofitClient {
    private const val BASE_URL = "https://api.quran.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: QuranApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(QuranApiService::class.java)
    }
}
