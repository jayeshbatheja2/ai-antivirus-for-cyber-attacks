package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.DialogProperties
import com.example.ui.AppTab
import com.example.ui.SecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: SecurityViewModel,
    historyList: List<com.example.database.entities.ScanHistory>,
    threatsList: List<com.example.database.entities.ActiveThreat>
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                windowInsets = WindowInsets.navigationBars
            ) {
                // Home Tab
                NavigationBarItem(
                    selected = viewModel.selectedTab == AppTab.HOME,
                    onClick = { viewModel.selectTab(AppTab.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (viewModel.selectedTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_home")
                )

                // History Tab
                NavigationBarItem(
                    selected = viewModel.selectedTab == AppTab.HISTORY,
                    onClick = { viewModel.selectTab(AppTab.HISTORY) },
                    icon = {
                        Icon(
                            imageVector = if (viewModel.selectedTab == AppTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_history")
                )

                // Threats Tab
                NavigationBarItem(
                    selected = viewModel.selectedTab == AppTab.THREATS,
                    onClick = { viewModel.selectTab(AppTab.THREATS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (threatsList.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(text = threatsList.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (viewModel.selectedTab == AppTab.THREATS) Icons.Filled.Security else Icons.Outlined.Security,
                                contentDescription = "Threats"
                            )
                        }
                    },
                    label = { Text("Threats", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_threats")
                )

                // Assistant Tab
                NavigationBarItem(
                    selected = viewModel.selectedTab == AppTab.ASSISTANT,
                    onClick = { viewModel.selectTab(AppTab.ASSISTANT) },
                    icon = {
                        Icon(
                            imageVector = if (viewModel.selectedTab == AppTab.ASSISTANT) Icons.Filled.SupportAgent else Icons.Outlined.SupportAgent,
                            contentDescription = "Assistant"
                        )
                    },
                    label = { Text("Assistant", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_assistant")
                )

                // Settings Tab
                NavigationBarItem(
                    selected = viewModel.selectedTab == AppTab.SETTINGS,
                    onClick = { viewModel.selectTab(AppTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (viewModel.selectedTab == AppTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Render Selected Screen Layout
            when (viewModel.selectedTab) {
                AppTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    threats = threatsList,
                    onNavigateToAssistant = { viewModel.selectTab(AppTab.ASSISTANT) }
                )
                AppTab.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    historyList = historyList
                )
                AppTab.THREATS -> ThreatCenterScreen(
                    viewModel = viewModel,
                    threats = threatsList
                )
                AppTab.ASSISTANT -> AssistantScreen(
                    viewModel = viewModel,
                    messages = viewModel.chatMessages.collectAsState().value
                )
                AppTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }

            // Interactive Scan Simulation Dialogue Overlay
            if (viewModel.isScanning) {
                Dialog(
                    onDismissRequest = {}, // Non-cancelable during scan execution
                    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Shield Scan Active",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Circular progress with active indicator
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(100.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { viewModel.scanProgress },
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 8.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                                Text(
                                    text = "${(viewModel.scanProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = viewModel.scanTargetName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Type: ${viewModel.scanTargetType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            // Dynamic diagnostics state
                            Text(
                                text = viewModel.scanStatusText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            )
                        }
                    }
                }
            }

            // Post-Scan Sim Outcomes Sheet Dialogue
            viewModel.lastSimulatedResult?.let { result ->
                ScanDetailDialog(
                    item = result,
                    onDismiss = { viewModel.dismissScanResult() }
                )
            }
        }
    }
}
