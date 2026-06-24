package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_versions")
data class ToolVersion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val toolId: Int,
    val version: Int,
    val htmlCode: String,
    val description: String, // "Initial Release", "Auto Repair Fix", etc.
    val createdAt: Long = System.currentTimeMillis()
)
