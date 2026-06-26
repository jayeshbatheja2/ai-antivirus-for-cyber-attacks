package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.database.entities.ScanHistory
import com.example.ui.SecurityViewModel
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.WarningAmber
import com.example.scanner.AnalysisEngine
import com.example.scanner.FileAnalysisResult
import com.example.scanner.ApkAnalysisResult
import com.example.scanner.PermissionRisk

@Composable
fun HistoryScreen(
    viewModel: SecurityViewModel,
    historyList: List<ScanHistory>,
    modifier: Modifier = Modifier
) {
    var selectedItemForDetail by remember { mutableStateOf<ScanHistory?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top Header Row with Clear Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Scan History",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Historical records of security analyses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            if (historyList.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearAllHistory() },
                    modifier = Modifier.testTag("clear_history_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Clear All History",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // List body
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryToggleOff,
                        contentDescription = "Empty History",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "History is Clean",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Trigger a scan on the Home Dashboard to save file, link, or system safety metrics here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        onClick = { selectedItemForDetail = item }
                    )
                }
            }
        }
    }

    // Explanatory Details Modal Dialog
    selectedItemForDetail?.let { item ->
        ScanDetailDialog(
            item = item,
            onDismiss = { selectedItemForDetail = null }
        )
    }
}

@Composable
fun HistoryItemCard(
    item: ScanHistory,
    onClick: () -> Unit
) {
    val statusColor = when (item.riskLevel.uppercase()) {
        "SAFE" -> SafeGreen
        "MEDIUM", "LOW" -> WarningAmber
        else -> DangerRed
    }

    val typeIcon = when (item.fileType.uppercase()) {
        "PDF" -> Icons.Default.PictureAsPdf
        "APK" -> Icons.Default.Android
        "URL" -> Icons.Default.Language
        "QR" -> Icons.Default.QrCode
        else -> Icons.Default.SettingsPhone
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Icon Background circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = item.fileType,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.fileType,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = DateUtils.getRelativeTimeSpanString(
                            item.timestamp,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        ).toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Score tag badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${item.riskScore}/100",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailDialog(
    item: ScanHistory,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isApk = item.fileType.uppercase() == "APK"

    Dialog(
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isApk) "APK REVERSE INTELLIGENCE" else "AI FILE INTELLIGENCE ENGINE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Security Audit Report",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Dialog")
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                // Scrollable content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isApk) {
                        // PHASE 3: APK Intelligence Report UI
                        val apkData = remember(item.title) {
                            AnalysisEngine.reverseEngineerApk(item.title)
                        }
                        ApkIntelligenceReportView(apkData)
                    } else {
                        // PHASE 2: File Intelligence Report UI
                        val fileData = remember(item.title, item.fileType) {
                            AnalysisEngine.analyzeFile(item.title, item.fileType)
                        }
                        FileIntelligenceReportView(fileData)
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close Audit")
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).testTag("dismiss_dialog_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.riskLevel.uppercase() == "SAFE") SafeGreen else DangerRed
                        )
                    ) {
                        Text("Acknowledge Risk")
                    }
                }
            }
        }
    }
}

@Composable
fun FileIntelligenceReportView(data: FileAnalysisResult) {
    val statusColor = when (data.riskLevel) {
        "SAFE" -> SafeGreen
        "LOW" -> WarningAmber
        "MEDIUM" -> WarningAmber
        "HIGH" -> DangerRed
        else -> DangerRed
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // OVERALL SUMMARY SECTION
        Card(
            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "File: ${data.fileName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = data.riskLevel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Risk Fusion Score:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${data.finalRiskScore} / 100",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                }

                LinearProgressIndicator(
                    progress = { data.finalRiskScore / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.15f)
                )

                Text(
                    text = data.aiExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // STEP 1: MAGIC BYTE DETECTION & EXTRA EXTENSION CHECKS
        SectionHeader("1. Magic Byte & Binary Engine")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BinaryKeyValueRow("Reported Extension:", data.fileType)
            BinaryKeyValueRow("Magic Bytes Discovered:", data.magicBytesDiscovered)
            BinaryKeyValueRow("Magic Bytes Expected:", data.magicBytes)
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = if (data.isMagicBytesMatch) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (data.isMagicBytesMatch) SafeGreen else DangerRed,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (data.isMagicBytesMatch) "Magic Bytes Align Perfectly with Extension." else "CRITICAL WARNING: Magic bytes do NOT match file extension! (Possible Spoofing)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (data.isMagicBytesMatch) SafeGreen else DangerRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // STEP 2 & 3: METADATA & STRUCTURE VALIDATOR
        SectionHeader("2. Metadata & Structure Verification")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            data.metadata.forEach { (k, v) ->
                BinaryKeyValueRow(k, v)
            }
            if (data.structureErrors.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(18.dp))
                    Text("File internal structure is standard and healthy.", style = MaterialTheme.typography.bodySmall, color = SafeGreen)
                }
            } else {
                data.structureErrors.forEach { error ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                        Text(error, style = MaterialTheme.typography.bodySmall, color = DangerRed)
                    }
                }
            }
        }

        // STEP 4 & 5: EMBEDDED CODE SCANNER & RECURSION ENGINE
        SectionHeader("3. Embedded Code & Dynamic Payload Scanner")
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (data.hasEmbeddedScript) Icons.Default.BugReport else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (data.hasEmbeddedScript) DangerRed else SafeGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (data.hasEmbeddedScript) "Active Scripts/Commands Discovered" else "No executable scripts or macros found",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (data.hasEmbeddedScript) DangerRed else SafeGreen
                    )
                }
                Text(
                    text = data.embeddedScriptDetails,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // STEP 6, 7 & 8: SIGNATURES, HASH & ENTROPY LEVEL
        SectionHeader("4. Binary Entropy & Signatures Diagnostic")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BinaryKeyValueRow("SHA-256 Binary Hash:", data.fileHash.take(16) + "..." + data.fileHash.takeLast(16))
            BinaryKeyValueRow("Entropy Score (Shannon):", "${data.entropy} bits/symbol")
            Text(
                text = "Entropy Level: ${data.entropyDescription}",
                style = MaterialTheme.typography.bodySmall,
                color = if (data.entropy > 7.0) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant
            )
            BinaryKeyValueRow("Database Signature Match:", if (data.signatureMatchFound) "FOUND: ${data.signatureName}" else "NONE (No matched signatures in offline definitions)")
        }

        // STEP 9 & 10: CLOUD REPUTATION & RECOMMENDATIONS
        SectionHeader("5. Cloud Intelligence & Action Plan")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (data.cloudReputationScore > 50) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (data.cloudReputationScore > 50) SafeGreen else DangerRed,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Cloud Reputation Score: ${data.cloudReputationScore}/100",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (data.cloudMaliciousCount > 0) {
                Text(
                    text = "Reported by ${data.cloudMaliciousCount} external security vendors (ThreatFox, VirusTotal feeds).",
                    style = MaterialTheme.typography.bodySmall,
                    color = DangerRed
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "RECOMMENDED ACTION:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = data.recommendation,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ApkIntelligenceReportView(data: ApkAnalysisResult) {
    val statusColor = when (data.riskLevel) {
        "SAFE" -> SafeGreen
        "LOW" -> WarningAmber
        "MEDIUM" -> WarningAmber
        "HIGH" -> DangerRed
        else -> DangerRed
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // OVERALL TRUST & RISK SUMMARY
        Card(
            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "App: ${data.appName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Trust Score: ${data.finalTrustScore}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                    }
                }

                BinaryKeyValueRow("Package Identifier:", data.packageName)
                BinaryKeyValueRow("SDK Profile:", "Min: ${data.minSdk} | Target: ${data.targetSdk}")

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (data.finalTrustScore > 70) Icons.Default.VerifiedUser else Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (data.finalTrustScore > 70) "Google Play Protect Equivalent: SAFE" else "PLAY PROTECT ALERT: Dynamic risk detected!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }

        // STEP 1 & 2: MANIFEST & PERMISSIONS MATRIX Weight scores
        SectionHeader("1. Permissions Danger Weight Score Matrix")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            data.permissionsMatrix.forEach { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.7f)) {
                        Text(
                            text = p.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (p.isRequested) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = p.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (p.isRequested) DangerRed.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (p.isRequested) "Score: ${p.riskScore}" else "Inactive",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (p.isRequested) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // STEP 3, 4 & 5: PERSISTENCE, ACTIVE SERVICES & BR
        SectionHeader("2. Active Background Components & Persistence")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BinaryKeyValueRow("Exported Components:", "${data.exportedComponentsCount} items (High risk if uncontrolled)")
            
            Text("Registered Background Services:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            data.activeServices.forEach { service ->
                Text(" • $service", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Auto-Start Broadcast Receivers:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            data.registeredReceivers.forEach { receiver ->
                Text(" • $receiver", style = MaterialTheme.typography.labelSmall, color = if (data.persistenceDetected) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (data.persistenceDetected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
                    Text("Auto-Boot completed persistence detected. The application can run automatically on reboot.", style = MaterialTheme.typography.bodySmall, color = DangerRed, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // STEP 6 & 7: ACCESSIBILITY ABUSE & SYSTEM_ALERT_WINDOW (OVERLAY)
        SectionHeader("3. Accessibility & Overlay Protection")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (data.accessibilityAbusePotential) Icons.Default.Cancel else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (data.accessibilityAbusePotential) DangerRed else SafeGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (data.accessibilityAbusePotential) "High risk accessibility crawl vector is requested" else "No accessibility abuse detected",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (data.accessibilityAbusePotential) DangerRed else SafeGreen
                )
            }
            if (data.accessibilityAbusePotential) {
                Text(
                    text = data.accessibilityAbuseReason,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (data.overlayPrivilegeActive) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (data.overlayPrivilegeActive) DangerRed else SafeGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (data.overlayPrivilegeActive) "Overlay privilege (SYSTEM_ALERT_WINDOW) active" else "No overlay vectors found",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (data.overlayPrivilegeActive) DangerRed else SafeGreen
                )
            }
        }

        // STEP 8, 9 & 10: NATIVE LIBRARIES, DEX REVERSE ANALYSIS & PROGUARD
        SectionHeader("4. Binary Decompiler & Native (.so) Audit")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BinaryKeyValueRow("Native Libraries Included:", data.nativeLibraries.joinToString(", "))
            BinaryKeyValueRow("Dynamic Reflection & JNI:", if (data.hasReflectionOrJni) "DETECTED: ${data.reflectionDetails}" else "NONE (Safe local execution model)")
            BinaryKeyValueRow("Obfuscation Diagnostics:", data.nativeObfuscationLevel)
            BinaryKeyValueRow("Proguard/DexGuard Checked:", if (data.proGuardApplied) "APPLIED (Standard compiler size optimization rules)" else "NOT FOUND (Raw class fields readable)")
        }

        // STEP 11, 12 & 13: HARDCODED SECRETS & C&C SERVER REPUTATION
        SectionHeader("5. Hardcoded Credentials & Server Scan")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (data.leakedSecrets.isEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(18.dp))
                    Text("No hardcoded AWS Keys, Firebase URLs, or client keys leaked.", style = MaterialTheme.typography.bodySmall, color = SafeGreen)
                }
            } else {
                Text("Leaked secrets found:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = DangerRed)
                data.leakedSecrets.forEach { secret ->
                    Text(" ❌ $secret", style = MaterialTheme.typography.labelSmall, color = DangerRed)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            BinaryKeyValueRow("Extracted Command Servers:", data.extractedUrls.joinToString(", "))
            BinaryKeyValueRow("Server IPs Discovered:", data.extractedIps.joinToString(", "))
            BinaryKeyValueRow("Autonomous System Reputation:", data.ASNReputation)
        }

        // STEP 14, 15 & 16: CERTIFICATE & DYNAMIC BEHAVIOR PREDICTOR AI
        SectionHeader("6. Certificate Status & Dynamic Behavior Prediction AI")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BinaryKeyValueRow("Certificate Signer:", data.certificateSigner)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (data.isDebugCertificate) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (data.isDebugCertificate) DangerRed else SafeGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (data.isDebugCertificate) "Uses Debug Keystore (Unsigned/Malicious release warning)" else "Official production key signature valid.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (data.isDebugCertificate) DangerRed else SafeGreen
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("AI predicted runtime behaviors if installed:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            data.predictedBehaviors.forEach { behavior ->
                Text(" ⚡ $behavior", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        // STEP 17 & 18: SIMULATOR & MALWARE FAMILY SIMILARITY
        SectionHeader("7. Install Simulator & Similarity Detection")
        Card(
            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Sandbox Install Simulation Report:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = statusColor)
                Text(
                    text = data.installationSimulationReport,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 15.sp
                )
                
                if (data.familySimilarityPercentage > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Malware Family Similarity:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DangerRed.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${data.familySimilarityName} (${data.familySimilarityPercentage}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 10.dp)
    )
}

@Composable
fun BinaryKeyValueRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}
