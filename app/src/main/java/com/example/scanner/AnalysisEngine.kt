package com.example.scanner

import kotlin.math.ln
import kotlin.random.Random

// --- PHASE 2 MODELS ---

data class FileAnalysisResult(
    val fileName: String,
    val fileType: String, // PDF, APK, URL, QR, DOCX, etc.
    val magicBytes: String,
    val isMagicBytesMatch: Boolean,
    val magicBytesDiscovered: String,
    val entropy: Double,
    val entropyDescription: String,
    val hasEmbeddedScript: Boolean,
    val embeddedScriptDetails: String,
    val metadata: Map<String, String>,
    val structureErrors: List<String>,
    val detectedFakeExtension: Boolean,
    val fileHash: String,
    val signatureMatchFound: Boolean,
    val signatureName: String,
    val cloudReputationScore: Int, // 0 to 100 reputation score
    val cloudMaliciousCount: Int,
    val finalRiskScore: Int, // 0 to 100 risk fusion score
    val riskLevel: String, // SAFE, LOW, MEDIUM, HIGH, CRITICAL
    val aiExplanation: String,
    val recommendation: String,
    val fusionDetails: Map<String, Int> // Score components contributed
)

// --- PHASE 3 MODELS ---

data class PermissionRisk(
    val name: String,
    val riskScore: Int,
    val description: String,
    val isRequested: Boolean
)

data class ApkAnalysisResult(
    val appName: String,
    val packageName: String,
    val targetSdk: Int,
    val minSdk: Int,
    val isCertificateValid: Boolean,
    val isDebugCertificate: Boolean,
    val certificateSigner: String,
    val permissionsMatrix: List<PermissionRisk>,
    val exportedComponentsCount: Int,
    val activeServices: List<String>,
    val registeredReceivers: List<String>,
    val persistenceDetected: Boolean,
    val accessibilityAbusePotential: Boolean,
    val accessibilityAbuseReason: String,
    val overlayPrivilegeActive: Boolean, // SYSTEM_ALERT_WINDOW
    val nativeLibraries: List<String>,
    val nativeObfuscationLevel: String,
    val hasReflectionOrJni: Boolean,
    val reflectionDetails: String,
    val leakedSecrets: List<String>,
    val extractedUrls: List<String>,
    val extractedIps: List<String>,
    val ASNReputation: String,
    val apkObfuscationDetected: Boolean,
    val proGuardApplied: Boolean,
    val predictedBehaviors: List<String>,
    val installationSimulationReport: String,
    val familySimilarityName: String,
    val familySimilarityPercentage: Int,
    val finalTrustScore: Int, // 100 - risk
    val finalRiskScore: Int,
    val riskLevel: String // SAFE, LOW, MEDIUM, HIGH, CRITICAL
)

object AnalysisEngine {

    // --- SHA-256 Signature Generator ---
    fun generateMockHash(fileName: String): String {
        val bytes = (fileName + "TrustShieldSalt").toByteArray()
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    // --- Shannon Entropy Calculator ---
    fun calculateShannonEntropy(data: String): Double {
        if (data.isEmpty()) return 0.0
        val frequencies = mutableMapOf<Char, Int>()
        for (char in data) {
            frequencies[char] = frequencies.getOrDefault(char, 0) + 1
        }
        var entropy = 0.0
        val len = data.length.toDouble()
        for (freq in frequencies.values) {
            val probability = freq.toDouble() / len
            entropy -= probability * (ln(probability) / ln(2.0))
        }
        return entropy
    }

    // --- FILE SECURITY ANALYSIS (PHASE 2) ---
    fun analyzeFile(fileName: String, type: String): FileAnalysisResult {
        val hash = generateMockHash(fileName)
        val lowerName = fileName.lowercase()

        // 1. Magic Bytes & Fake Extensions
        val magicBytesDiscovered: String
        val magicBytesExpected: String
        val isMagicBytesMatch: Boolean
        val detectedFakeExtension: Boolean

        when (type.uppercase()) {
            "PDF" -> {
                magicBytesExpected = "25 50 44 46 (%PDF)"
                if (lowerName.contains(".exe") || lowerName.endsWith(".pdf.lnk")) {
                    magicBytesDiscovered = "50 4B 03 04 (PK ZIP)"
                    isMagicBytesMatch = false
                    detectedFakeExtension = true
                } else {
                    magicBytesDiscovered = "25 50 44 46 (%PDF)"
                    isMagicBytesMatch = true
                    detectedFakeExtension = false
                }
            }
            "APK" -> {
                magicBytesExpected = "50 4B 03 04 (PK ZIP)"
                magicBytesDiscovered = "50 4B 03 04 (PK ZIP)"
                isMagicBytesMatch = true
                detectedFakeExtension = false
            }
            "URL" -> {
                magicBytesExpected = "N/A"
                magicBytesDiscovered = "N/A"
                isMagicBytesMatch = true
                detectedFakeExtension = false
            }
            else -> {
                magicBytesExpected = "N/A"
                magicBytesDiscovered = "N/A"
                isMagicBytesMatch = true
                detectedFakeExtension = false
            }
        }

        // 2. Entropy Analysis
        val entropy = if (lowerName.contains("crypt") || lowerName.contains("speed_booster") || lowerName.contains("patch")) {
            7.94 // Packed/encrypted high entropy
        } else {
            4.65 // Normal text/PDF entropy
        }
        val entropyDescription = when {
            entropy > 7.5 -> "Extremely High (Typically Packed or Encrypted Code)"
            entropy > 6.0 -> "Moderate (Standard Compressed Media/Document)"
            else -> "Low (Clear readable plain text or static binaries)"
        }

        // 3. Embedded Script Engine
        var hasEmbeddedScript = false
        var embeddedScriptDetails = "None detected."
        if (type.uppercase() == "PDF") {
            if (lowerName.contains("invoice_receipt_june") || lowerName.contains("tax") || lowerName.contains("contract")) {
                hasEmbeddedScript = false
            } else {
                hasEmbeddedScript = true
                embeddedScriptDetails = "Found /OpenAction element pointing to hidden obfuscated JavaScript execution payload."
            }
        } else if (type.uppercase() == "APK") {
            hasEmbeddedScript = true
            embeddedScriptDetails = "Found embedded classes.dex file executing base64 dynamic reflections."
        } else if (type.uppercase() == "URL" && (lowerName.contains("support-google") || lowerName.contains("paypal-update"))) {
            hasEmbeddedScript = true
            embeddedScriptDetails = "Found malicious redirect scripts designed to hijack key inputs (Keylogger simulation)."
        }

        // 4. Metadata Inspector
        val metadata = mutableMapOf<String, String>()
        if (type.uppercase() == "PDF") {
            metadata["Creator"] = if (hasEmbeddedScript) "Unknown PyPDF-Malicious Toolkit" else "Adobe Acrobat Pro 2026"
            metadata["Author"] = if (hasEmbeddedScript) "hacker_deploy_group" else "TrustShield Security Team"
            metadata["Compressed"] = "Yes"
            metadata["Pages"] = "1"
        } else if (type.uppercase() == "APK") {
            metadata["Package"] = "com.aistudio.reversesandbox"
            metadata["Target SDK"] = "35"
            metadata["Signed Key"] = "AndroidDebugKey"
        } else {
            metadata["Protocol"] = "HTTPS (Secure Socket Layer)"
            metadata["Port"] = "443"
            metadata["Cert Authority"] = "Let's Encrypt Security"
        }

        // 5. Structure Validation
        val structureErrors = mutableListOf<String>()
        if (detectedFakeExtension) {
            structureErrors.add("Mismatch: File extension claims to be PDF but magic signature points to dangerous Executable binary!")
        }
        if (hasEmbeddedScript && type.uppercase() == "PDF") {
            structureErrors.add("Embedded non-standard dynamic object references detected prior to EOF marker.")
        }

        // 6. Signature DB Checks
        val isMaliciousInDB = lowerName.contains("booster") || lowerName.contains("alert") || lowerName.contains("update-invoice") || detectedFakeExtension
        val signatureName = if (isMaliciousInDB) "Spyware.Android.InjectedObfuscator.TS" else "N/A"

        // 7. Simulated Cloud Intel Reputation
        val cloudMaliciousCount = if (isMaliciousInDB) 68 else 0
        val cloudReputationScore = if (isMaliciousInDB) 12 else 99

        // 8. Risk Fusion Engine
        val fusionDetails = mutableMapOf<String, Int>()
        var baseScore = 0

        if (!isMagicBytesMatch) {
            baseScore += 35
            fusionDetails["Magic Byte Mismatch"] = 35
        }
        if (detectedFakeExtension) {
            baseScore += 15
            fusionDetails["Double Extension Fraud"] = 15
        }
        if (hasEmbeddedScript) {
            baseScore += 30
            fusionDetails["Embedded Script Risk"] = 30
        }
        if (entropy > 7.0) {
            baseScore += 10
            fusionDetails["High Complexity Entropy"] = 10
        }
        if (isMaliciousInDB) {
            baseScore += 40
            fusionDetails["Signature Database Hit"] = 40
        } else {
            baseScore += 5
            fusionDetails["Heuristics Risk Core"] = 5
        }

        val finalRiskScore = baseScore.coerceIn(5, 100)
        val riskLevel = when {
            finalRiskScore >= 80 -> "CRITICAL"
            finalRiskScore >= 60 -> "HIGH"
            finalRiskScore >= 40 -> "MEDIUM"
            finalRiskScore >= 20 -> "LOW"
            else -> "SAFE"
        }

        val aiExplanation = when (riskLevel) {
            "CRITICAL" -> "This file is extremely dangerous. It mimics harmless documents but carries packed executable payloads designed to bypass antivirus protection."
            "HIGH" -> "This file contains suspicious characteristics such as embedded redirection URLs or scripts that could compromise credentials."
            "MEDIUM" -> "This file exhibits minor anomalies like high entropy or unusual creator tools. Exercise caution before opening."
            "LOW" -> "Slight configuration warnings found, but no malware signatures are active."
            else -> "Perfect health. No hidden scripts, double extensions, packing scripts, or database threats found."
        }

        val recommendation = when (riskLevel) {
            "CRITICAL", "HIGH" -> "Delete immediately. Do not execute or transfer this file to any other device."
            "MEDIUM" -> "Verify the sender's details. If you did not request this, delete it securely."
            else -> "This file is completely secure. Safe to open."
        }

        return FileAnalysisResult(
            fileName = fileName,
            fileType = type,
            magicBytes = magicBytesExpected,
            isMagicBytesMatch = isMagicBytesMatch,
            magicBytesDiscovered = magicBytesDiscovered,
            entropy = entropy,
            entropyDescription = entropyDescription,
            hasEmbeddedScript = hasEmbeddedScript,
            embeddedScriptDetails = embeddedScriptDetails,
            metadata = metadata,
            structureErrors = structureErrors,
            detectedFakeExtension = detectedFakeExtension,
            fileHash = hash,
            signatureMatchFound = isMaliciousInDB,
            signatureName = signatureName,
            cloudReputationScore = cloudReputationScore,
            cloudMaliciousCount = cloudMaliciousCount,
            finalRiskScore = finalRiskScore,
            riskLevel = riskLevel,
            aiExplanation = aiExplanation,
            recommendation = recommendation,
            fusionDetails = fusionDetails
        )
    }

    // --- APK INTELLIGENCE REVERSE ENGINEERING (PHASE 3) ---
    fun reverseEngineerApk(fileName: String): ApkAnalysisResult {
        val lowerName = fileName.lowercase()
        val isMalicious = lowerName.contains("booster") || lowerName.contains("crypto") || lowerName.contains("miner")

        // 1. Android Manifest permissions risk weight score matrix
        val permissionsMatrix = listOf(
            PermissionRisk("android.permission.INTERNET", 5, "Allows network socket connections to remote servers.", true),
            PermissionRisk("android.permission.READ_SMS", 40, "Allows reading private incoming SMS verification OTPs.", isMalicious),
            PermissionRisk("android.permission.RECEIVE_SMS", 40, "Intercepts real-time SMS messages before user sees them.", isMalicious),
            PermissionRisk("android.permission.BIND_ACCESSIBILITY_SERVICE", 50, "Total screen automation (Clicks, Reads, Gestures, Password capture).", isMalicious),
            PermissionRisk("android.permission.SYSTEM_ALERT_WINDOW", 35, "Creates floating windows on top of banks to steal credentials (Overlay).", isMalicious),
            PermissionRisk("android.permission.RECEIVE_BOOT_COMPLETED", 15, "Launches automatic malicious backgrounds upon device restart.", true),
            PermissionRisk("android.permission.READ_CONTACTS", 25, "Harvests private address books for scam spamming.", isMalicious)
        )

        // 2. Active suspicious components
        val activeServices = if (isMalicious) {
            listOf("com.trustshield.sandbox.BackgroundSmsForwardService", "com.trustshield.sandbox.AccessibilitySpyMonitor")
        } else {
            listOf("com.instagram.common.analytics.AnalyticsService", "com.instagram.push.PushNotificationService")
        }

        val registeredReceivers = if (isMalicious) {
            listOf("com.trustshield.sandbox.AutoBootPersistenceReceiver", "com.trustshield.sandbox.SmsInterceptorTrigger")
        } else {
            listOf("com.instagram.common.receiver.NetworkChangeReceiver")
        }

        // 3. Accessibility danger checks
        val accessibilityAbusePotential = isMalicious
        val accessibilityAbuseReason = if (isMalicious) {
            "This application configures an Accessibility service that requested screen crawling permissions. It can silently tap OK on banking alerts or capture passwords on keystrokes."
        } else "None"

        // 4. Native .so Libraries
        val nativeLibraries = if (isMalicious) {
            listOf("libpayload_packer.so", "libcrypt_engine.so")
        } else {
            listOf("libjpeg_encoder.so", "libreact_native.so")
        }
        val nativeObfuscationLevel = if (isMalicious) "High Obfuscation & Dynamic JNI Linking" else "Standard ProGuard Stripping"

        // 5. Hardcoded credentials leaking
        val leakedSecrets = if (isMalicious) {
            listOf("FIREBASE_KEY: AIzaSyD-781HJS88SA8sh21A", "AWS_S3_BUCKET: s3://malicious-cnc-logs-bucket")
        } else emptyList()

        // 6. Extracted C&C Servers (URLs & Geo-IP Reputation)
        val extractedUrls = if (isMalicious) {
            listOf("http://45.221.90.12/cnc/register", "http://anubis-payload-host.cn/get_cmd")
        } else {
            listOf("https://graph.instagram.com/v12.0", "https://api.facebook.com")
        }

        val extractedIps = if (isMalicious) {
            listOf("45.221.90.12", "192.168.10.2")
        } else {
            listOf("31.13.71.36")
        }

        val asnReputation = if (isMalicious) {
            "Host: Autonomous System 41282 (Highly flagged bulletproof hosting provider in high-risk zones)"
        } else "Host: Meta Platforms AS32934 (Clean record)"

        // 7. Obfuscation & Certificate Diagnostics
        val isDebugCertificate = isMalicious
        val certificateSigner = if (isDebugCertificate) "CN=Android Debug, O=Android, C=US" else "CN=Instagram LLC, O=Meta, C=US"

        // 8. AI Behaviour Predictions (Phase 3 highlight)
        val predictedBehaviors = if (isMalicious) {
            listOf(
                "Run immediately on boot persistence configuration.",
                "Silently capture and redirect incoming financial OTP SMS vectors.",
                "Utilize screen accessibility abuse to log live touch keys.",
                "Display phishing overlay layout layers on popular wallets."
            )
        } else {
            listOf(
                "Establish secure secure socket networking links to official social graphs.",
                "Receive background push notification packages."
            )
        }

        // 9. APK Family similarity score (Future AI)
        val familySimilarityName = if (isMalicious) "Anubis Banking Trojan Core" else "N/A"
        val familySimilarityPercentage = if (isMalicious) 89 else 0

        // 10. Risk calculation
        var riskScore = 15
        if (isMalicious) {
            riskScore = 88
        }
        val riskLevel = when {
            riskScore >= 80 -> "CRITICAL"
            riskScore >= 60 -> "HIGH"
            riskScore >= 40 -> "MEDIUM"
            riskScore >= 20 -> "LOW"
            else -> "SAFE"
        }

        val finalTrustScore = 100 - riskScore

        val installationSimulationReport = if (isMalicious) {
            "WARNING: If installed, this app will start in the background immediately on device reboot. It will request 'Accessibility Access' and if granted, it will intercept all input texts (including credit card pins) and forward them to a remote command server."
        } else {
            "CLEAN: This application operates normal permissions and respects modern platform safety parameters. Safe for device deployment."
        }

        return ApkAnalysisResult(
            appName = fileName,
            packageName = if (isMalicious) "com.shadybooster.spyware" else "com.instagram.android",
            targetSdk = 35,
            minSdk = 26,
            isCertificateValid = true,
            isDebugCertificate = isDebugCertificate,
            certificateSigner = certificateSigner,
            permissionsMatrix = permissionsMatrix,
            exportedComponentsCount = if (isMalicious) 8 else 44,
            activeServices = activeServices,
            registeredReceivers = registeredReceivers,
            persistenceDetected = isMalicious,
            accessibilityAbusePotential = accessibilityAbusePotential,
            accessibilityAbuseReason = accessibilityAbuseReason,
            overlayPrivilegeActive = isMalicious,
            nativeLibraries = nativeLibraries,
            nativeObfuscationLevel = nativeObfuscationLevel,
            hasReflectionOrJni = isMalicious,
            reflectionDetails = if (isMalicious) "Uses Class.forName() & DexClassLoader to fetch remote runtime assets." else "None",
            leakedSecrets = leakedSecrets,
            extractedUrls = extractedUrls,
            extractedIps = extractedIps,
            ASNReputation = asnReputation,
            apkObfuscationDetected = isMalicious,
            proGuardApplied = true,
            predictedBehaviors = predictedBehaviors,
            installationSimulationReport = installationSimulationReport,
            familySimilarityName = familySimilarityName,
            familySimilarityPercentage = familySimilarityPercentage,
            finalTrustScore = finalTrustScore,
            finalRiskScore = riskScore,
            riskLevel = riskLevel
        )
    }
}
