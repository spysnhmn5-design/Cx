package com.example.data

import kotlinx.coroutines.flow.Flow

class ToolRepository(private val toolDao: ToolDao) {
    val allTools: Flow<List<Tool>> = toolDao.getAllTools()

    suspend fun getToolById(toolId: Int): Tool? = toolDao.getToolById(toolId)

    suspend fun insertTool(tool: Tool): Long = toolDao.insertTool(tool)

    suspend fun updateTool(tool: Tool) = toolDao.updateTool(tool)

    suspend fun deleteTool(toolId: Int) {
        toolDao.deleteToolById(toolId)
        toolDao.deleteVersionsByToolId(toolId)
    }

    fun getVersionsForToolFlow(toolId: Int): Flow<List<ToolVersion>> =
        toolDao.getVersionsForToolFlow(toolId)

    suspend fun getVersionsForTool(toolId: Int): List<ToolVersion> =
        toolDao.getVersionsForTool(toolId)

    suspend fun getLatestVersionForTool(toolId: Int): ToolVersion? =
        toolDao.getLatestVersionForTool(toolId)

    suspend fun insertVersion(version: ToolVersion): Long =
        toolDao.insertVersion(version)
}
