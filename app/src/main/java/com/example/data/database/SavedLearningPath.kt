package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.LearningPath

@Entity(tableName = "saved_learning_paths")
data class SavedLearningPath(
    @PrimaryKey val topicId: String, // lowercase cleaned topic name, e.g. "python"
    val topic: String, // formatted title, e.g. "Python Programming"
    val learningPath: LearningPath,
    val savedAt: Long = System.currentTimeMillis()
)
