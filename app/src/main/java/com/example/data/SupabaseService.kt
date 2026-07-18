package com.example.data

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class SupabaseCourse(
    val topic_id: String,
    val topic: String,
    val learning_path_json: String
)

interface SupabaseApi {
    @GET("rest/v1/cached_courses")
    suspend fun getCourse(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("topic_id") topicId: String
    ): List<SupabaseCourse>

    @POST("rest/v1/cached_courses")
    suspend fun insertCourse(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Body course: SupabaseCourse
    ): retrofit2.Response<Unit>
}

object SupabaseService {
    private const val TAG = "SupabaseService"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Initialize Retrofit lazily and safely
    private val api: SupabaseApi? by lazy {
        val url = BuildConfig.SUPABASE_URL
        val anonKey = BuildConfig.SUPABASE_ANON_KEY

        if (url.isBlank() || url.contains("your-project") || anonKey.isBlank() || anonKey.contains("your-anon-key")) {
            Log.w(TAG, "Supabase credentials are not configured. Online caching is disabled.")
            null
        } else {
            try {
                // Ensure base URL ends with a slash and does not contain /rest/v1
                var cleanUrl = url.trim()
                if (cleanUrl.endsWith("/rest/v1/")) {
                    cleanUrl = cleanUrl.removeSuffix("/rest/v1/")
                } else if (cleanUrl.endsWith("/rest/v1")) {
                    cleanUrl = cleanUrl.removeSuffix("/rest/v1")
                }
                val baseUrl = if (cleanUrl.endsWith("/")) cleanUrl else "$cleanUrl/"
                
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()

                retrofit.create(SupabaseApi::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Supabase Retrofit client", e)
                null
            }
        }
    }

    val isEnabled: Boolean
        get() = api != null

    private val anonKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY

    private val authHeader: String
        get() = "Bearer $anonKey"

    /**
     * Tries to find a cached course in Supabase by topic name.
     * Returns the parsed LearningPath if found, or null otherwise.
     */
    suspend fun getCachedCourse(topic: String): LearningPath? {
        val activeApi = api ?: return null
        val topicId = topic.lowercase().trim()
        val queryParam = "eq.$topicId"

        return try {
            val response = activeApi.getCourse(
                apiKey = anonKey,
                authorization = authHeader,
                topicId = queryParam
            )
            val match = response.firstOrNull() ?: return null
            Log.d(TAG, "Cache hit on Supabase for topic: $topic")
            moshi.adapter(LearningPath::class.java).fromJson(match.learning_path_json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get cached course from Supabase for $topic", e)
            null
        }
    }

    /**
     * Caches a newly generated course to Supabase.
     */
    suspend fun cacheCourse(topic: String, learningPath: LearningPath) {
        val activeApi = api ?: return
        val topicId = topic.lowercase().trim()

        try {
            val jsonText = moshi.adapter(LearningPath::class.java).toJson(learningPath)
            val supabaseCourse = SupabaseCourse(
                topic_id = topicId,
                topic = topic,
                learning_path_json = jsonText
            )
            val response = activeApi.insertCourse(
                apiKey = anonKey,
                authorization = authHeader,
                course = supabaseCourse
            )
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully cached course on Supabase for: $topic")
            } else {
                Log.w(TAG, "Failed to cache course on Supabase. Code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache course on Supabase for $topic", e)
        }
    }
}
