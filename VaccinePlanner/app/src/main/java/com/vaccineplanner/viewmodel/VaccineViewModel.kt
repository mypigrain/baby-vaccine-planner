package com.vaccineplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.vaccineplanner.data.model.*
import com.vaccineplanner.data.repository.VaccineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class VaccineViewModel(application: Application) : AndroidViewModel(application) {
    
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, object : TypeAdapter<LocalDate>() {
            private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            override fun write(out: JsonWriter, value: LocalDate?) {
                if (value == null) {
                    out.nullValue()
                } else {
                    out.value(value.format(formatter))
                }
            }
            override fun read(reader: JsonReader): LocalDate? {
                val value = reader.nextString()
                return if (value.isNullOrEmpty()) null else LocalDate.parse(value, formatter)
            }
        })
        .create()
    
    private val prefsFile = File(application.filesDir, "vaccine_data.json")
    
    private val _baby = MutableStateFlow<Baby?>(null)
    val baby: StateFlow<Baby?> = _baby.asStateFlow()
    
    private val _schedules = MutableStateFlow<List<VaccinationRecord>>(emptyList())
    val schedules: StateFlow<List<VaccinationRecord>> = _schedules.asStateFlow()
    
    private val _selectedPaidVaccines = MutableStateFlow<List<Vaccine>>(emptyList())
    val selectedPaidVaccines: StateFlow<List<Vaccine>> = _selectedPaidVaccines.asStateFlow()
    
    private val _currentScreen = MutableStateFlow<Screen>(Screen.BabyInfo)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    
    private val _previousScreen = MutableStateFlow<Screen?>(null)
    val previousScreen: StateFlow<Screen?> = _previousScreen.asStateFlow()
    
    private val _detailSourceScreen = MutableStateFlow<Screen?>(null)
    val detailSourceScreen: StateFlow<Screen?> = _detailSourceScreen.asStateFlow()
    
    private val _navigationDirection = MutableStateFlow(1)
    val navigationDirection: StateFlow<Int> = _navigationDirection.asStateFlow()
    
    private val _scheduleScrollPosition = MutableStateFlow(0)
    val scheduleScrollPosition: StateFlow<Int> = _scheduleScrollPosition.asStateFlow()
    
    private val _paidVaccineScrollPosition = MutableStateFlow(0)
    val paidVaccineScrollPosition: StateFlow<Int> = _paidVaccineScrollPosition.asStateFlow()
    
    private val _selectedVaccineForDetail = MutableStateFlow<Vaccine?>(null)
    val selectedVaccineForDetail: StateFlow<Vaccine?> = _selectedVaccineForDetail.asStateFlow()
    
    init {
        loadData()
    }
    
    fun setBabyInfo(name: String, birthDate: LocalDate) {
        _baby.value = Baby(
            id = System.currentTimeMillis().toString(),
            name = name,
            birthDate = birthDate
        )
        generateSchedule()
        saveData()
    }
    
    fun generateSchedule() {
        val baby = _baby.value ?: return
        val newSchedules = VaccineRepository.generateSchedule(
            baby.birthDate,
            _selectedPaidVaccines.value
        )
        
        val completedVaccineIds = _schedules.value
            .filter { it.isCompleted }
            .map { "${it.vaccine.id}_${it.doseNumber}" }
            .toSet()
        
        _schedules.value = newSchedules.map { record ->
            val recordKey = "${record.vaccine.id}_${record.doseNumber}"
            if (recordKey in completedVaccineIds) {
                record.copy(isCompleted = true, completedDate = LocalDate.now())
            } else {
                record
            }
        }
    }
    
    fun markVaccinationCompleted(recordId: String) {
        _schedules.value = _schedules.value.map { record ->
            if (record.id == recordId) {
                record.copy(isCompleted = true, completedDate = LocalDate.now())
            } else {
                record
            }
        }
        saveData()
    }
    
    fun markVaccinationIncomplete(recordId: String) {
        _schedules.value = _schedules.value.map { record ->
            if (record.id == recordId) {
                record.copy(isCompleted = false, completedDate = null)
            } else {
                record
            }
        }
        saveData()
    }
    
    fun addPaidVaccine(vaccine: Vaccine) {
        if (!_selectedPaidVaccines.value.any { it.id == vaccine.id }) {
            _selectedPaidVaccines.value = _selectedPaidVaccines.value + vaccine
            generateSchedule()
            saveData()
        }
    }
    
    fun removePaidVaccine(vaccine: Vaccine) {
        _selectedPaidVaccines.value = _selectedPaidVaccines.value.filter { it.id != vaccine.id }
        generateSchedule()
        saveData()
    }
    
    fun isPaidVaccineSelected(vaccineId: String): Boolean {
        return _selectedPaidVaccines.value.any { it.id == vaccineId }
    }
    
    fun navigateTo(screen: Screen) {
        _navigationDirection.value = if (screen.ordinal > _currentScreen.value.ordinal) 1 else -1
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = screen
    }
    
    fun selectVaccineForDetail(vaccine: Vaccine?) {
        _selectedVaccineForDetail.value = vaccine
    }
    
    fun saveScheduleScrollPosition(position: Int) {
        _scheduleScrollPosition.value = position
    }
    
    fun savePaidVaccineScrollPosition(position: Int) {
        _paidVaccineScrollPosition.value = position
    }
    
    fun navigateToDetail(fromScreen: Screen, vaccine: Vaccine) {
        _navigationDirection.value = 1
        _previousScreen.value = fromScreen
        _detailSourceScreen.value = fromScreen
        _selectedVaccineForDetail.value = vaccine
        _currentScreen.value = Screen.VaccineDetail
    }
    
    fun getFreeVaccines(): List<Vaccine> = VaccineRepository.freeVaccines
    
    fun getPaidVaccines(): List<Vaccine> = VaccineRepository.paidVaccines
    
    fun getVaccineById(id: String): Vaccine? = VaccineRepository.getVaccineById(id)
    
    private fun saveData() {
        viewModelScope.launch {
            try {
                val data = VaccineData(
                    baby = _baby.value,
                    schedules = _schedules.value,
                    selectedPaidVaccines = _selectedPaidVaccines.value
                )
                prefsFile.writeText(gson.toJson(data))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                if (prefsFile.exists()) {
                    val json = prefsFile.readText()
                    val type = object : TypeToken<VaccineData>() {}.type
                    val data: VaccineData = gson.fromJson(json, type)
                    _baby.value = data.baby
                    _schedules.value = data.schedules
                    _selectedPaidVaccines.value = data.selectedPaidVaccines
                    
                    if (_baby.value != null) {
                        _currentScreen.value = Screen.VaccineSchedule
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun resetAll() {
        _baby.value = null
        _schedules.value = emptyList()
        _selectedPaidVaccines.value = emptyList()
        _currentScreen.value = Screen.BabyInfo
        prefsFile.delete()
    }
}

data class VaccineData(
    val baby: Baby?,
    val schedules: List<VaccinationRecord>,
    val selectedPaidVaccines: List<Vaccine>
)

sealed class Screen(val ordinal: Int) {
    object BabyInfo : Screen(0)
    object VaccineSchedule : Screen(1)
    object PaidVaccineList : Screen(2)
    object VaccineDetail : Screen(3)
}
