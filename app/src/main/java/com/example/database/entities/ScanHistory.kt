package com.example.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val fileType: String, // "PDF", "APK", "URL", "QR", "DEVICE"
    val riskLevel: String, // "SAFE", "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val riskScore: Int, // e.g. 95 (Safe) or 15 (Critical)
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val statusDetails: String
)
