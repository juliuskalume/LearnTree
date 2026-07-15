package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

interface GroqApi {
    @POST("openai/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

object GroqService {
    private const val BASE_URL = "https://api.groq.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: GroqApi = retrofit.create(GroqApi::class.java)

    private val apiKey: String
        get() = "Bearer ${BuildConfig.GROQ_API_KEY}"

    suspend fun generateLearningPath(topic: String): LearningPath? {
        val systemPrompt = """
            You are an expert curriculum designer. Your task is to generate a comprehensive learning path for the topic: "$topic".
            You MUST return ONLY a JSON object matching this structure:
            {
              "topic": "$topic",
              "estimatedHours": "Estimated hours (e.g., 30 Hours)",
              "description": "Short description of the curriculum",
              "modules": [
                {
                  "moduleNumber": 1,
                  "moduleTitle": "Module title",
                  "lessons": [
                    {
                      "lessonNumber": 1,
                      "lessonTitle": "Lesson title",
                      "level": "Beginner/Intermediate/Advanced",
                      "duration": "Estimated duration (e.g., 4h)",
                      "shortDescription": "One-sentence overview",
                      "content": "Detailed text content of the lesson explaining concepts, with some markdown format if needed.",
                      "aiTip": "A funny or helpful tip/analogy",
                      "keyConcept": "Core concept summary",
                      "realWorldExample": "How this is used in the real world",
                      "quizQuestion": "A multiple choice question on this lesson",
                      "quizOptions": ["Option A", "Option B", "Option C"],
                      "quizCorrectIndex": 0
                    }
                  ]
                }
              ]
            }
            Do NOT include any markdown code blocks, preamble or explanation. Output ONLY valid JSON.
        """.trimIndent()

        val messages = listOf(
            ChatMessage(role = "system", content = systemPrompt),
            ChatMessage(role = "user", content = "Generate curriculum for: $topic")
        )

        val request = ChatCompletionRequest(
            model = "llama-3.3-70b-versatile",
            messages = messages,
            temperature = 0.5,
            response_format = ResponseFormat(type = "json_object")
        )

        return try {
            val response = api.getChatCompletion(apiKey, request)
            val jsonText = response.choices.firstOrNull()?.message?.content ?: return null
            moshi.adapter(LearningPath::class.java).fromJson(jsonText)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getChatResponse(messages: List<ChatMessage>): String {
        val request = ChatCompletionRequest(
            model = "llama-3.3-70b-versatile",
            messages = messages,
            temperature = 0.7
        )
        return try {
            val response = api.getChatCompletion(apiKey, request)
            response.choices.firstOrNull()?.message?.content ?: "Error: Empty response"
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.localizedMessage ?: "Unknown network error"}"
        }
    }

    suspend fun generateDetailedLesson(topic: String, moduleTitle: String, lessonTitle: String, lessonNumber: Int): Lesson? {
        val systemPrompt = """
            You are an expert AI tutor. Your task is to generate highly detailed and comprehensive lesson content and a corresponding multiple-choice quiz for the lesson: "$lessonTitle" within the module: "$moduleTitle" for the topic: "$topic".
            You MUST return ONLY a JSON object matching this structure:
            {
              "lessonNumber": $lessonNumber,
              "lessonTitle": "$lessonTitle",
              "level": "Beginner",
              "duration": "10m",
              "shortDescription": "One-sentence overview",
              "content": "A highly detailed, step-by-step tutorial on this lesson with examples, formatting it cleanly with markdown if helpful.",
              "aiTip": "A memorable or helpful tip/analogy",
              "keyConcept": "Core concept summary of what was taught",
              "realWorldExample": "How this specific concept is used in real-world industries or apps",
              "quizQuestion": "A high-quality multiple choice question testing understanding of this specific lesson content",
              "quizOptions": ["Option A", "Option B", "Option C", "Option D"],
              "quizCorrectIndex": 0
            }
            Do NOT include any markdown code blocks (such as ```json), preamble, or explanation. Output ONLY valid JSON.
        """.trimIndent()

        val messages = listOf(
            ChatMessage(role = "system", content = systemPrompt),
            ChatMessage(role = "user", content = "Generate detailed content and quiz for lesson: $lessonTitle")
        )

        val request = ChatCompletionRequest(
            model = "llama-3.3-70b-versatile",
            messages = messages,
            temperature = 0.6,
            response_format = ResponseFormat(type = "json_object")
        )

        return try {
            val response = api.getChatCompletion(apiKey, request)
            val jsonText = response.choices.firstOrNull()?.message?.content ?: return null
            moshi.adapter(Lesson::class.java).fromJson(jsonText)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
