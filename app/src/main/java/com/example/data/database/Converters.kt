package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.LearningPath
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(LearningPath::class.java)

    @TypeConverter
    fun fromLearningPath(path: LearningPath?): String? {
        return path?.let { adapter.toJson(it) }
    }

    @TypeConverter
    fun toLearningPath(json: String?): LearningPath? {
        return json?.let { adapter.fromJson(it) }
    }
}
