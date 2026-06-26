package com.example.database

import com.example.database.dao.ActiveThreatDao
import com.example.database.dao.ScanHistoryDao
import com.example.database.entities.ActiveThreat
import com.example.database.entities.ScanHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SecurityRepository(
    private val scanHistoryDao: ScanHistoryDao,
    private val activeThreatDao: ActiveThreatDao
) {
    val allHistory: Flow<List<ScanHistory>> = scanHistoryDao.getAllHistory()
    val allThreats: Flow<List<ActiveThreat>> = activeThreatDao.getAllThreats()

    suspend fun insertHistory(item: ScanHistory) {
        scanHistoryDao.insertHistory(item)
    }

    suspend fun deleteHistoryById(id: Int) {
        scanHistoryDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        scanHistoryDao.clearAllHistory()
    }

    suspend fun insertThreat(threat: ActiveThreat) {
        activeThreatDao.insertThreat(threat)
    }

    suspend fun deleteThreatById(id: Int) {
        activeThreatDao.deleteThreatById(id)
    }

    suspend fun clearThreats() {
        activeThreatDao.clearAllThreats()
    }

    suspend fun seedInitialDataIfNeeded() {
        val currentHistory = allHistory.first()
        if (currentHistory.isEmpty()) {
            val now = System.currentTimeMillis()
            // Add premium mock histories
            scanHistoryDao.insertHistory(
                ScanHistory(
                    title = "invoice_receipt_june.pdf",
                    fileType = "PDF",
                    riskLevel = "SAFE",
                    riskScore = 98,
                    timestamp = now - 3600000, // 1 hr ago
                    description = "Secure Document",
                    statusDetails = "This file contains standard PDF elements with no hidden script payloads, suspicious outbound redirects, or embedded malicious forms."
                )
            )
            scanHistoryDao.insertHistory(
                ScanHistory(
                    title = "speed_booster_pro.apk",
                    fileType = "APK",
                    riskLevel = "CRITICAL",
                    riskScore = 12,
                    timestamp = now - 86400000, // 1 day ago
                    description = "Spyware Threat Detected",
                    statusDetails = "This application is dangerous because it contains disguised packer structures and requests severe background screen capture & keylogging overlay privileges."
                )
            )
            scanHistoryDao.insertHistory(
                ScanHistory(
                    title = "http://secure-bank-login.xyz/pay",
                    fileType = "URL",
                    riskLevel = "HIGH",
                    riskScore = 25,
                    timestamp = now - 172800000, // 2 days ago
                    description = "Phishing Redirect Link",
                    statusDetails = "This link impersonates a popular financial institution and attempts to capture user credentials via deceptive form submission."
                )
            )
            scanHistoryDao.insertHistory(
                ScanHistory(
                    title = "Workplace_Wifi_Hotspot",
                    fileType = "QR",
                    riskLevel = "LOW",
                    riskScore = 80,
                    timestamp = now - 259200000, // 3 days ago
                    description = "Unencrypted WiFi Config QR",
                    statusDetails = "Scanning this QR attempts to configure an unencrypted WiFi connection. Use with caution."
                )
            )
        }

        val currentThreats = allThreats.first()
        if (currentThreats.isEmpty()) {
            val now = System.currentTimeMillis()
            // Add threat center risks
            activeThreatDao.insertThreat(
                ActiveThreat(
                    title = "Unsecured Public Wi-Fi Connected",
                    threatType = "WIFI",
                    riskLevel = "HIGH",
                    description = "You are currently connected to 'Airport_Free_Wifi' which does not require a strong secure password. This leaves your local device data open to interception.",
                    suggestion = "Disconnect immediately, use a secure cellular network, or launch a trusted system VPN.",
                    timestamp = now
                )
            )
            activeThreatDao.insertThreat(
                ActiveThreat(
                    title = "USB Debugging Active",
                    threatType = "SETTING",
                    riskLevel = "MEDIUM",
                    description = "Developer Options and USB Debugging are active. Anyone gaining physical control over your device could sideload malicious tracking scripts.",
                    suggestion = "Go to System Settings -> Developer Options and toggle 'USB Debugging' off.",
                    timestamp = now - 1800000 // 30 min ago
                )
            )
            activeThreatDao.insertThreat(
                ActiveThreat(
                    title = "Weak Lock Screen Security",
                    threatType = "LOCKSCREEN",
                    riskLevel = "HIGH",
                    description = "Your phone lock method is configured with a weak pattern. Device data is vulnerable to local physical breaches.",
                    suggestion = "Setup biometrics or upgrade your screen security to a strong 6-digit numeric PIN.",
                    timestamp = now - 7200000 // 2 hrs ago
                )
            )
        }
    }
}
