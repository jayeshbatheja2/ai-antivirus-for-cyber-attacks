package com.example.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_threats")
data class ActiveThreat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val threatType: String, // "WIFI", "SETTING", "APP", "LOCKSCREEN"
    val riskLevel: String, // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val description: String,
    val suggestion: String,
    val timestamp: Long = System.currentTimeMillis()
)
