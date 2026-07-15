package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    val id: String,
    val email: String?
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    val access_token: String,
    val refresh_token: String?,
    val user: SupabaseUser
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class UserProgress(
    val user_id: String,
    val email: String,
    val xp: Int,
    val streak: Int,
    val saved_paths_json: String
)

interface SupabaseAuthApi {
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Header("apikey") apiKey: String,
        @Body request: SupabaseAuthRequest
    ): Response<SupabaseAuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(
        @Header("apikey") apiKey: String,
        @Body request: SupabaseAuthRequest
    ): Response<SupabaseAuthResponse>

    @GET("rest/v1/user_progress")
    suspend fun getProgress(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Query("user_id") userId: String
    ): List<UserProgress>

    @POST("rest/v1/user_progress")
    suspend fun upsertProgress(
        @Header("apikey") apiKey: String,
        @Header("Authorization") authorization: String,
        @Header("Prefer") prefer: String = "resolution=merge-duplicates",
        @Body progress: UserProgress
    ): Response<Unit>
}

object SupabaseAuthService {
    private const val TAG = "SupabaseAuthService"
    private const val PREFS_NAME = "supabase_auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val api: SupabaseAuthApi? by lazy {
        val url = BuildConfig.SUPABASE_URL
        if (url.isBlank() || url.contains("your-project")) {
            Log.w(TAG, "Supabase URL not configured.")
            null
        } else {
            try {
                val baseUrl = if (url.endsWith("/")) url else "$url/"
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                val okHttpClient = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()

                Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
                    .create(SupabaseAuthApi::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to init auth Retrofit client", e)
                null
            }
        }
    }

    private val _currentUser = MutableStateFlow<SupabaseUser?>(null)
    val currentUser: StateFlow<SupabaseUser?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private var accessToken: String? = null
    private var sharedPrefs: android.content.SharedPreferences? = null

    fun init(context: Context) {
        sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedToken = sharedPrefs?.getString(KEY_ACCESS_TOKEN, null)
        val savedId = sharedPrefs?.getString(KEY_USER_ID, null)
        val savedEmail = sharedPrefs?.getString(KEY_USER_EMAIL, null)

        if (savedToken != null && savedId != null) {
            accessToken = savedToken
            _currentUser.value = SupabaseUser(id = savedId, email = savedEmail)
            Log.d(TAG, "Session restored for user: $savedEmail")
        }
    }

    val isSessionActive: Boolean
        get() = _currentUser.value != null

    val isEnabled: Boolean
        get() = api != null

    suspend fun signUp(email: String, password: String): Boolean {
        val activeApi = api ?: return false
        _isLoading.value = true
        _authError.value = null

        return try {
            val response = activeApi.signUp(BuildConfig.SUPABASE_ANON_KEY, SupabaseAuthRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                saveSession(data)
                _isLoading.value = false
                true
            } else {
                val errBody = response.errorBody()?.string() ?: "Sign Up failed"
                Log.e(TAG, "Signup Error: $errBody")
                _authError.value = "Sign Up failed: ${parseErrorMessage(errBody)}"
                _isLoading.value = false
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during signup", e)
            _authError.value = "Network error: ${e.localizedMessage ?: "Unknown error"}"
            _isLoading.value = false
            false
        }
    }

    suspend fun signIn(email: String, password: String): Boolean {
        val activeApi = api ?: return false
        _isLoading.value = true
        _authError.value = null

        return try {
            val response = activeApi.signIn(BuildConfig.SUPABASE_ANON_KEY, SupabaseAuthRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                saveSession(data)
                _isLoading.value = false
                true
            } else {
                val errBody = response.errorBody()?.string() ?: "Login failed"
                Log.e(TAG, "Login Error: $errBody")
                _authError.value = "Login failed: ${parseErrorMessage(errBody)}"
                _isLoading.value = false
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during login", e)
            _authError.value = "Network error: ${e.localizedMessage ?: "Unknown error"}"
            _isLoading.value = false
            false
        }
    }

    fun signOut() {
        accessToken = null
        _currentUser.value = null
        sharedPrefs?.edit()?.clear()?.apply()
        Log.d(TAG, "User logged out, session cleared")
    }

    private fun saveSession(data: SupabaseAuthResponse) {
        accessToken = data.access_token
        _currentUser.value = data.user
        sharedPrefs?.edit()?.apply {
            putString(KEY_ACCESS_TOKEN, data.access_token)
            putString(KEY_USER_ID, data.user.id)
            putString(KEY_USER_EMAIL, data.user.email)
            apply()
        }
    }

    private fun parseErrorMessage(errorJson: String): String {
        return try {
            // Very simple JSON string extraction for error message
            if (errorJson.contains("\"msg\"")) {
                errorJson.substringAfter("\"msg\":\"").substringBefore("\"")
            } else if (errorJson.contains("\"error_description\"")) {
                errorJson.substringAfter("\"error_description\":\"").substringBefore("\"")
            } else {
                errorJson
            }
        } catch (e: Exception) {
            errorJson
        }
    }

    suspend fun syncProgress(xp: Int, streak: Int, savedPathsJson: String) {
        val activeApi = api ?: return
        val user = _currentUser.value ?: return
        val authHeader = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}"

        try {
            val progress = UserProgress(
                user_id = user.id,
                email = user.email ?: "",
                xp = xp,
                streak = streak,
                saved_paths_json = savedPathsJson
            )
            val response = activeApi.upsertProgress(
                apiKey = BuildConfig.SUPABASE_ANON_KEY,
                authorization = authHeader,
                progress = progress
            )
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully synced progress to Supabase")
            } else {
                Log.w(TAG, "Failed to sync progress. Code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception syncing progress", e)
        }
    }

    suspend fun fetchProgress(): UserProgress? {
        val activeApi = api ?: return null
        val user = _currentUser.value ?: return null
        val authHeader = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}"

        return try {
            val list = activeApi.getProgress(
                apiKey = BuildConfig.SUPABASE_ANON_KEY,
                authorization = authHeader,
                userId = "eq.${user.id}"
            )
            list.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching progress", e)
            null
        }
    }
}
