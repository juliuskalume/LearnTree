package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<ChatMessage>,
    val temperature: Double? = 0.7,
    val response_format: ResponseFormat? = null
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    val type: String // e.g. "json_object"
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    val choices: List<Choice>
)

@JsonClass(generateAdapter = true)
data class Choice(
    val message: ChatMessage
)

// Dynamic Learning Path models parsed from JSON output of Groq
@JsonClass(generateAdapter = true)
data class Lesson(
    val lessonNumber: Int,
    val lessonTitle: String,
    val level: String,
    val duration: String,
    val shortDescription: String,
    val content: String,
    val aiTip: String,
    val keyConcept: String,
    val realWorldExample: String,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val quizCorrectIndex: Int
)

@JsonClass(generateAdapter = true)
data class Module(
    val moduleNumber: Int,
    val moduleTitle: String,
    val lessons: List<Lesson>
)

@JsonClass(generateAdapter = true)
data class LearningPath(
    val topic: String,
    val estimatedHours: String,
    val description: String,
    val modules: List<Module>
)
