package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolDao {
    @Query("SELECT * FROM tools ORDER BY createdAt DESC")
    fun getAllTools(): Flow<List<Tool>>

    @Query("SELECT * FROM tools WHERE id = :toolId")
    suspend fun getToolById(toolId: Int): Tool?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: Tool): Long

    @Update
    suspend fun updateTool(tool: Tool)

    @Query("DELETE FROM tools WHERE id = :toolId")
    suspend fun deleteToolById(toolId: Int)

    // Version management
    @Query("SELECT * FROM tool_versions WHERE toolId = :toolId ORDER BY version DESC")
    fun getVersionsForToolFlow(toolId: Int): Flow<List<ToolVersion>>

    @Query("SELECT * FROM tool_versions WHERE toolId = :toolId ORDER BY version DESC")
    suspend fun getVersionsForTool(toolId: Int): List<ToolVersion>

    @Query("SELECT * FROM tool_versions WHERE toolId = :toolId ORDER BY version DESC LIMIT 1")
    suspend fun getLatestVersionForTool(toolId: Int): ToolVersion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: ToolVersion): Long

    @Query("DELETE FROM tool_versions WHERE toolId = :toolId")
    suspend fun deleteVersionsByToolId(toolId: Int)
}
