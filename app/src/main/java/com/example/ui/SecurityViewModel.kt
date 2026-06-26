package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.database.AppDatabase
import com.example.database.SecurityRepository
import com.example.database.entities.ActiveThreat
import com.example.database.entities.ScanHistory
import com.example.scanner.AnalysisEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppTab {
    HOME, HISTORY, THREATS, ASSISTANT, SETTINGS
}

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class SecurityViewModel(
    application: Application,
    private val repository: SecurityRepository
) : AndroidViewModel(application) {

    // Tab Navigation
    var selectedTab by mutableStateOf(AppTab.HOME)
        private set

    fun selectTab(tab: AppTab) {
        selectedTab = tab
    }

    // Local DB Observers
    val scanHistoryList: StateFlow<List<ScanHistory>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeThreatsList: StateFlow<List<ActiveThreat>> = repository.allThreats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Scanning Simulation State
    var isScanning by mutableStateOf(false)
        private set
    var scanProgress by mutableStateOf(0f)
        private set
    var scanTargetName by mutableStateOf("")
        private set
    var scanTargetType by mutableStateOf("")
        private set
    var scanStatusText by mutableStateOf("")
        private set
    var lastSimulatedResult by mutableStateOf<ScanHistory?>(null)

    // Chatbot Messages List
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                text = "Hello! I am your TrustShield Security Advisor. Ask me anything about files, safe links, device security, or suspicious messages."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Settings States
    var isDarkTheme by mutableStateOf(true)
        private set
    var isCloudScanEnabled by mutableStateOf(false)
        private set
    var isNotificationsEnabled by mutableStateOf(true)
        private set

    init {
        // Seed some highly informative security data on first open
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    // Settings actions
    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }

    fun toggleCloudScan(enabled: Boolean) {
        isCloudScanEnabled = enabled
    }

    fun toggleNotifications(enabled: Boolean) {
        isNotificationsEnabled = enabled
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            repository.clearHistory()
            repository.clearThreats()
            repository.seedInitialDataIfNeeded()
        }
    }

    // Resolve active threats
    fun resolveThreat(threatId: Int) {
        viewModelScope.launch {
            repository.deleteThreatById(threatId)
        }
    }

    // Simulation Scan Trigger
    fun triggerScan(targetType: String) {
        if (isScanning) return
        
        viewModelScope.launch {
            isScanning = true
            scanProgress = 0f
            scanTargetType = targetType
            lastSimulatedResult = null

            val sampleTargets = when (targetType) {
                "PDF" -> listOf("update_instructions_v3.pdf", "tax_statement_2026.pdf", "housing_contract.pdf")
                "APK" -> listOf("super_charger_game.apk", "direct_messenger_v9.apk", "crypto_wallet_miner.apk")
                "URL" -> listOf("http://support-google-alert.xyz/verify", "https://paypal-update-invoice.com", "https://github.com/kotlin")
                "QR" -> listOf("Guest_Wifi_Connect_QR", "Cafe_Coupons_Promo_QR", "Payment_Scanner_Code")
                else -> listOf("Full System Deep Scan", "System Configuration Check", "Active Background Memory Services")
            }
            
            scanTargetName = sampleTargets.random()
            
            val statusMessages = listOf(
                "Initializing local TrustShield scan engine...",
                "Analysing system file structures & signatures...",
                "Querying local hashes database...",
                "Running heuristic scanner on dynamic packages...",
                "Scanning dynamic linking libraries & permissions...",
                "Verifying source validation certificate authorities...",
                "Structuring safety results..."
            )

            for (i in 0 until statusMessages.size) {
                scanStatusText = statusMessages[i]
                val steps = 5
                for (step in 1..steps) {
                    delay(100 + Random.nextLong(100))
                    scanProgress += 1f / (statusMessages.size * steps)
                }
            }

            scanProgress = 1f
            delay(300)

            // Randomize safety outcome
            val result = generateRandomScanResult(scanTargetName, targetType)
            repository.insertHistory(result)
            lastSimulatedResult = result
            
            isScanning = false
        }
    }

    private fun generateRandomScanResult(name: String, type: String): ScanHistory {
        if (type.uppercase() == "APK") {
            val apkData = AnalysisEngine.reverseEngineerApk(name)
            return ScanHistory(
                title = name,
                fileType = type,
                riskLevel = apkData.riskLevel,
                riskScore = apkData.finalTrustScore,
                description = if (apkData.riskLevel == "SAFE") "Secure App Package" else "Risk App Package",
                statusDetails = "Predicted behavior: " + apkData.predictedBehaviors.joinToString("; ")
            )
        } else {
            val fileData = AnalysisEngine.analyzeFile(name, type)
            return ScanHistory(
                title = name,
                fileType = type,
                riskLevel = fileData.riskLevel,
                riskScore = 100 - fileData.finalRiskScore,
                description = if (fileData.riskLevel == "SAFE") "Secure Object Detected" else "Potential Security Risk",
                statusDetails = "AI Explanation: " + fileData.aiExplanation
            )
        }
    }

    // Close Scan Simulation Details Overlay
    fun dismissScanResult() {
        lastSimulatedResult = null
    }

    // Chatbot interactions
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        
        val userMsg = ChatMessage(sender = "user", text = text)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            delay(800) // Simulation thinking latency
            val responseText = getCybersecurityResponse(text)
            val aiMsg = ChatMessage(sender = "ai", text = responseText)
            _chatMessages.value = _chatMessages.value + aiMsg
        }
    }

    private fun getCybersecurityResponse(query: String): String {
        val normalized = query.lowercase().trim()
        return when {
            normalized.contains("pdf") -> {
                "Generally, PDF files are safe, but they can carry malicious redirects or embedded scripts. Never open unexpected PDF files from unknown emails. Always check them with TrustShield before opening."
            }
            normalized.contains("apk") -> {
                "Installing APK files from browsers (sideloading) bypasses Google Play Protect. Many infected APKs contain background Trojan horse scripts designed to steal banking OTPs. Install from trusted stores whenever possible."
            }
            normalized.contains("scam") || normalized.contains("sms") || normalized.contains("message") || normalized.contains("link") -> {
                "Scam messages usually trigger artificial urgency (e.g., 'Account locked! Click here to pay'). Never enter credit cards, PINs, or OTPs on links sent via SMS. If unsure, open the official app directly."
            }
            normalized.contains("permission") || normalized.contains("camera") || normalized.contains("location") -> {
                "App permissions govern what private data an app can access. If a simple calculator app asks for Location, SMS, and Camera, deny them. Only grant permissions relevant to the app's actual core function."
            }
            normalized.contains("wifi") || normalized.contains("hotspot") -> {
                "Unsecured public Wi-Fi lack encryption. Bad actors on the same network can capture passwords or intercept communications using 'man-in-the-middle' tools. Use cellular data or launch a VPN on public Wi-Fi."
            }
            normalized.contains("hello") || normalized.contains("hi") || normalized.contains("hey") -> {
                "Hello! How can I help secure your digital life today? Ask me about safe browser habits, phone locks, or suspicious file types."
            }
            else -> {
                "Excellent security question. For safety, remember these core rules: 1) Avoid unknown download links. 2) Never share SMS code OTPs with anybody. 3) Keep your Android system updated to receive the latest security patches. Is there a specific issue you are experiencing?"
            }
        }
    }

    // Custom Factory for ViewModel Construction
    class Factory(
        private val application: Application,
        private val repository: SecurityRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SecurityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SecurityViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
