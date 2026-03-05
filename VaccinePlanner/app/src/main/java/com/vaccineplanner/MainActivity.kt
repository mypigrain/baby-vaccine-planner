package com.vaccineplanner

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.vaccineplanner.ui.components.VaccineSelectionDialog
import com.vaccineplanner.ui.screens.*
import com.vaccineplanner.ui.theme.VaccinePlannerTheme
import com.vaccineplanner.viewmodel.Screen
import com.vaccineplanner.viewmodel.VaccineViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.attributes = window.attributes.apply {
            preferredDisplayModeId = display?.supportedModes?.find { it.refreshRate >= 120 }?.modeId 
                ?: window.attributes.preferredDisplayModeId
        }
        
        enableEdgeToEdge()
        
        val viewModel = VaccineViewModel(application)
        
        setContent {
            VaccinePlannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val baby by viewModel.baby.collectAsState()
                    val schedules by viewModel.schedules.collectAsState()
                    val selectedPaidVaccines by viewModel.selectedPaidVaccines.collectAsState()
                    val currentScreen by viewModel.currentScreen.collectAsState()
                    val selectedVaccineForDetail by viewModel.selectedVaccineForDetail.collectAsState()
                    val deepseekApiKey by viewModel.deepseekApiKey.collectAsState()
                    val lastOverallAnalysisResult by viewModel.lastOverallAnalysisResult.collectAsState()
                    val lastCurrentMonthAnalysisResult by viewModel.lastCurrentMonthAnalysisResult.collectAsState()
                    val lastVaccineInfoResult by viewModel.lastVaccineInfoResult.collectAsState()
                    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
                    val streamingContent by viewModel.streamingContent.collectAsState()
                    val analysisError by viewModel.analysisError.collectAsState()
                    
                    var mainListState by remember { mutableStateOf(mapOf<String, Int>()) }
                    var showVaccineSelectionDialog by remember { mutableStateOf(false) }
                    var analysisType by remember { mutableStateOf<String?>(null) }
                    
                    val coroutineScope = rememberCoroutineScope()
                    var showErrorDialog by remember { mutableStateOf(false) }
                    var errorMessage by remember { mutableStateOf("") }
                    var shouldShowConfigButton by remember { mutableStateOf(false) }
                    
                    val backPressHandler = {
                        when (currentScreen) {
                            is Screen.BabyInfo -> {
                                finish()
                            }
                            is Screen.VaccineSchedule -> {
                                moveTaskToBack(true)
                            }
                            is Screen.PaidVaccineList -> {
                                viewModel.navigateTo(Screen.VaccineSchedule)
                            }
                            is Screen.VaccineDetail -> {
                                viewModel.navigateTo(viewModel.detailSourceScreen.value ?: Screen.VaccineSchedule)
                            }
                            is Screen.AISettings -> {
                                viewModel.navigateTo(Screen.VaccineSchedule)
                            }
                            is Screen.AIAnalysisResult -> {
                                viewModel.navigateTo(Screen.VaccineSchedule)
                            }
                            is Screen.DonateScreen -> {
                                viewModel.navigateTo(Screen.VaccineSchedule)
                            }
                        }
                    }
                    
                    this@MainActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            backPressHandler()
                        }
                    })
                    
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            val direction = if (initialState.ordinal < targetState.ordinal) 1 else -1
                            (slideInHorizontally { width -> direction * width } + fadeIn(animationSpec = tween(durationMillis = 350)))
                                .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut(animationSpec = tween(durationMillis = 350)))
                        },
                        contentKey = { it.javaClass.simpleName },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                        is Screen.BabyInfo -> {
                            BabyInfoScreen(
                                onSubmit = { name, birthDate ->
                                    viewModel.setBabyInfo(name, birthDate)
                                    viewModel.navigateTo(Screen.VaccineSchedule)
                                }
                            )
                        }
                        
                        is Screen.VaccineSchedule -> {
                            if (baby != null) {
                                val currentDate by viewModel.currentDate.collectAsState()
                                VaccineScheduleScreen(
                                    baby = baby!!,
                                    schedules = schedules,
                                    selectedPaidVaccines = selectedPaidVaccines,
                                    currentDate = currentDate,
                                    onMarkCompleted = { viewModel.markVaccinationCompleted(it) },
                                    onMarkIncomplete = { viewModel.markVaccinationIncomplete(it) },
                                    onNavigateToPaidVaccines = { viewModel.navigateTo(Screen.PaidVaccineList) },
                                    onNavigateToVaccineDetail = { vaccine ->
                                        viewModel.navigateToDetail(Screen.VaccineSchedule, vaccine)
                                    },
                                    onAIOverallAnalysis = {
                                        analysisType = "overall"
                                        viewModel.navigateTo(Screen.AIAnalysisResult)
                                    },
                                    onAICurrentMonthAnalysis = {
                                        analysisType = "currentMonth"
                                        viewModel.navigateTo(Screen.AIAnalysisResult)
                                    },
                                    onAIVaccineInfoQuery = {
                                        showVaccineSelectionDialog = true
                                    },
                                    onNavigateToAISettings = { viewModel.navigateTo(Screen.AISettings) },
                                    onNavigateToDonate = { viewModel.navigateTo(Screen.DonateScreen) },
                                    onReset = { viewModel.resetAll() },
                                    savedScrollPosition = mainListState["schedule"] ?: 0,
                                    onSaveScrollPosition = { position ->
                                        mainListState = mainListState.toMutableMap().also {
                                            it["schedule"] = position
                                        }
                                    }
                                )
                            }
                        }
                        
                        is Screen.PaidVaccineList -> {
                            PaidVaccineListScreen(
                                paidVaccines = viewModel.getPaidVaccines(),
                                selectedVaccines = selectedPaidVaccines,
                                onVaccineSelect = { viewModel.addPaidVaccine(it) },
                                onVaccineDeselect = { viewModel.removePaidVaccine(it) },
                                onViewDetail = { vaccine ->
                                    viewModel.navigateToDetail(Screen.PaidVaccineList, vaccine)
                                },
                                onNavigateBack = { viewModel.navigateTo(Screen.VaccineSchedule) },
                                savedScrollPosition = mainListState["paid"] ?: 0,
                                onSaveScrollPosition = { position ->
                                    mainListState = mainListState.toMutableMap().also {
                                        it["paid"] = position
                                    }
                                }
                            )
                        }
                        
                        is Screen.VaccineDetail -> {
                            selectedVaccineForDetail?.let { vaccine ->
                                VaccineDetailScreen(
                                    vaccine = vaccine,
                                    isSelected = viewModel.isPaidVaccineSelected(vaccine.id),
                                    onAddToSchedule = { viewModel.addPaidVaccine(vaccine) },
                                    onRemoveFromSchedule = { viewModel.removePaidVaccine(vaccine) },
                                    onNavigateBack = { 
                                        viewModel.selectVaccineForDetail(null)
                                        viewModel.navigateTo(viewModel.detailSourceScreen.value ?: Screen.VaccineSchedule)
                                    }
                                )
                            }
                        }
                        
                        is Screen.AISettings -> {
                            AISettingsScreen(
                                apiKey = deepseekApiKey,
                                onSaveApiKey = { viewModel.setDeepseekApiKey(it) },
                                onNavigateBack = { viewModel.navigateTo(Screen.VaccineSchedule) }
                            )
                        }
                        
                        is Screen.AIAnalysisResult -> {
                            val title = when (analysisType) {
                                "overall" -> "整体计划分析"
                                "currentMonth" -> "当月接种分析"
                                "vaccineInfo" -> "疫苗详细信息"
                                else -> "AI分析结果"
                            }
                            
                            val content = when (analysisType) {
                                "overall" -> lastOverallAnalysisResult
                                "currentMonth" -> lastCurrentMonthAnalysisResult
                                "vaccineInfo" -> lastVaccineInfoResult
                                else -> null
                            }
                            
                            AIAnalysisResultScreen(
                                title = title,
                                content = content,
                                streamingContent = streamingContent,
                                isLoading = isAnalyzing,
                                onNavigateBack = { 
                                    analysisType = null
                                    viewModel.navigateTo(Screen.VaccineSchedule) 
                                },
                                onRegenerate = if (analysisType == "overall" || analysisType == "currentMonth") {
                                    {
                                        coroutineScope.launch {
                                            when (analysisType) {
                                                "overall" -> viewModel.performOverallAnalysis(regenerate = true)
                                                "currentMonth" -> viewModel.performCurrentMonthAnalysis(regenerate = true)
                                            }
                                        }
                                    }
                                } else null
                            )
                        }
                        
                        is Screen.DonateScreen -> {
                            DonateScreen(
                                onNavigateBack = { viewModel.navigateTo(Screen.VaccineSchedule) }
                            )
                        }
                    }
                    }
                    
                    LaunchedEffect(analysisType) {
                        if (analysisType != null) {
                            coroutineScope.launch {
                                when (analysisType) {
                                    "overall" -> viewModel.performOverallAnalysis(regenerate = false)
                                    "currentMonth" -> viewModel.performCurrentMonthAnalysis(regenerate = false)
                                }
                            }
                        }
                    }
                    
                    LaunchedEffect(analysisError) {
                        if (analysisError != null) {
                            errorMessage = analysisError ?: "未知错误"
                            
                            // Check if error is related to API key
                            shouldShowConfigButton = analysisError?.let { 
                                it.contains("API Key") || 
                                it.contains("401") || 
                                it.contains("invalid") || 
                                it.contains("expired") ||
                                it.contains("请先配置")
                            } ?: false
                            
                            if (shouldShowConfigButton) {
                                showErrorDialog = true
                            } else {
                                // Show toast for other errors
                                android.widget.Toast.makeText(
                                    this@MainActivity,
                                    errorMessage,
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                viewModel.clearAnalysisError()
                            }
                        }
                    }
                    
                    if (showVaccineSelectionDialog) {
                        VaccineSelectionDialog(
                            vaccines = viewModel.getPaidVaccines(),
                            onDismiss = { showVaccineSelectionDialog = false },
                            onVaccineSelected = { vaccine ->
                                // Close dialog first, then navigate after animation completes
                                showVaccineSelectionDialog = false
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(100) // Wait for dialog dismiss animation
                                    analysisType = "vaccineInfo"
                                    viewModel.navigateTo(Screen.AIAnalysisResult)
                                    viewModel.performVaccineInfoQuery(vaccine)
                                }
                            }
                        )
                    }
                    
                    if (showErrorDialog) {
                        AlertDialog(
                            onDismissRequest = { showErrorDialog = false; viewModel.clearAnalysisError() },
                            icon = {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            title = { Text("API Key配置错误") },
                            text = {
                                Text(errorMessage)
                            },
                            confirmButton = {
                                TextButton(onClick = { 
                                    showErrorDialog = false
                                    viewModel.clearAnalysisError()
                                }) {
                                    Text("确定")
                                }
                            },
                            dismissButton = if (shouldShowConfigButton) {
                                {
                                    TextButton(onClick = { 
                                        showErrorDialog = false
                                        viewModel.clearAnalysisError()
                                        viewModel.navigateTo(Screen.AISettings)
                                    }) {
                                        Text("前往配置")
                                    }
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}
