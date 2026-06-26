package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.database.AppDatabase
import com.example.database.SecurityRepository
import com.example.ui.SecurityViewModel
import com.example.ui.screens.MainDashboard
import com.example.ui.theme.TrustShieldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize offline SQLite database
        val database = AppDatabase.getDatabase(applicationContext)
        
        // 2. Setup abstracted repository
        val repository = SecurityRepository(
            scanHistoryDao = database.scanHistoryDao(),
            activeThreatDao = database.activeThreatDao()
        )
        
        // 3. Setup core system ViewModels with factories
        val viewModelFactory = SecurityViewModel.Factory(application, repository)
        val viewModel: SecurityViewModel by viewModels { viewModelFactory }
        
        // 4. Configure full edge-to-edge system drawing
        enableEdgeToEdge()
        
        // 5. Set reactive content theme
        setContent {
            TrustShieldTheme(darkTheme = viewModel.isDarkTheme) {
                val historyList by viewModel.scanHistoryList.collectAsState()
                val threatsList by viewModel.activeThreatsList.collectAsState()
                
                MainDashboard(
                    viewModel = viewModel,
                    historyList = historyList,
                    threatsList = threatsList
                )
            }
        }
    }
}
