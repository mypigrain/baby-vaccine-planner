package com.vaccineplanner

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
                    
                    val backPressHandler = {
                        when (currentScreen) {
                            is Screen.BabyInfo -> {
                                finish()
                            }
                            is Screen.VaccineSchedule -> {
                                finish()
                            }
                            is Screen.PaidVaccineList -> {
                                viewModel.navigateTo(Screen.VaccineSchedule)
                            }
                            is Screen.VaccineDetail -> {
                                viewModel.selectVaccineForDetail(null)
                                viewModel.navigateTo(Screen.PaidVaccineList)
                            }
                        }
                    }
                    
                    this@MainActivity.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            backPressHandler()
                        }
                    })
                    
                    when (currentScreen) {
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
                                VaccineScheduleScreen(
                                    baby = baby!!,
                                    schedules = schedules,
                                    selectedPaidVaccines = selectedPaidVaccines,
                                    onMarkCompleted = { viewModel.markVaccinationCompleted(it) },
                                    onMarkIncomplete = { viewModel.markVaccinationIncomplete(it) },
                                    onNavigateToPaidVaccines = { viewModel.navigateTo(Screen.PaidVaccineList) },
                                    onNavigateToVaccineDetail = { vaccine ->
                                        viewModel.selectVaccineForDetail(vaccine)
                                        viewModel.navigateTo(Screen.VaccineDetail)
                                    },
                                    onReset = { viewModel.resetAll() }
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
                                    viewModel.selectVaccineForDetail(vaccine)
                                    viewModel.navigateTo(Screen.VaccineDetail)
                                },
                                onNavigateBack = { viewModel.navigateTo(Screen.VaccineSchedule) }
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
                                        viewModel.navigateTo(viewModel.previousScreen.value ?: Screen.VaccineSchedule)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
