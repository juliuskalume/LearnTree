package com.example.data.database

import com.example.data.LearningPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LearningPathRepository(private val dao: LearningPathDao) {
    val allSavedPaths: Flow<List<LearningPath>> = dao.getAllSavedPaths().map { entities ->
        entities.map { it.learningPath }
    }

    suspend fun insert(topic: String, learningPath: LearningPath) {
        val entity = SavedLearningPath(
            topicId = topic.lowercase().trim(),
            topic = topic,
            learningPath = learningPath
        )
        dao.insertPath(entity)
    }

    suspend fun delete(topic: String) {
        dao.deletePathById(topic.lowercase().trim())
    }
}
