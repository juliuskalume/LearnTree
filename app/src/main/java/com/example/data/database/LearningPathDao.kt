package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningPathDao {
    @Query("SELECT * FROM saved_learning_paths ORDER BY savedAt DESC")
    fun getAllSavedPaths(): Flow<List<SavedLearningPath>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPath(path: SavedLearningPath)

    @Query("DELETE FROM saved_learning_paths WHERE topicId = :topicId")
    suspend fun deletePathById(topicId: String)

    @Query("DELETE FROM saved_learning_paths")
    suspend fun clearAll()
}
