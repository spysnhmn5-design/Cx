package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tools")
data class Tool(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val prompt: String,
    val icon: String, // Emoji or icon code
    val status: String, // "ACTIVE", "ERROR", "NEEDS_INTERNET"
    val isOfflinePossible: Boolean,
    val explanation: String,
    val createdAt: Long = System.currentTimeMillis()
)
