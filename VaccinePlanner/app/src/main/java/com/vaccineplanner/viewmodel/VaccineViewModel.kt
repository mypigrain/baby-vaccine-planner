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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
    
    private val _scheduleScrollPosition = MutableStateFlow(0)
    val scheduleScrollPosition: StateFlow<Int> = _scheduleScrollPosition.asStateFlow()
    
    private val _paidVaccineScrollPosition = MutableStateFlow(0)
    val paidVaccineScrollPosition: StateFlow<Int> = _paidVaccineScrollPosition.asStateFlow()
    
    private val _selectedVaccineForDetail = MutableStateFlow<Vaccine?>(null)
    val selectedVaccineForDetail: StateFlow<Vaccine?> = _selectedVaccineForDetail.asStateFlow()
    
    private val _deepseekApiKey = MutableStateFlow<String>("")
    val deepseekApiKey: StateFlow<String> = _deepseekApiKey.asStateFlow()
    
    private val _lastOverallAnalysisResult = MutableStateFlow<String?>(null)
    val lastOverallAnalysisResult: StateFlow<String?> = _lastOverallAnalysisResult.asStateFlow()
    
    private val _lastCurrentMonthAnalysisResult = MutableStateFlow<String?>(null)
    val lastCurrentMonthAnalysisResult: StateFlow<String?> = _lastCurrentMonthAnalysisResult.asStateFlow()
    
    private val _lastVaccineInfoResult = MutableStateFlow<String?>(null)
    val lastVaccineInfoResult: StateFlow<String?> = _lastVaccineInfoResult.asStateFlow()
    
    private val _isAnalyzing = MutableStateFlow<Boolean>(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    
    private val _streamingContent = MutableStateFlow<String?>(null)
    val streamingContent: StateFlow<String?> = _streamingContent.asStateFlow()
    
    private val _analysisError = MutableStateFlow<String?>(null)
    val analysisError: StateFlow<String?> = _analysisError.asStateFlow()
    
    private val _currentDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()
    
    private val _scheduleCache = MutableStateFlow<ScheduleCache?>(null)
    val scheduleCache: StateFlow<ScheduleCache?> = _scheduleCache.asStateFlow()
    
    private val _isScheduleLoading = MutableStateFlow(true)
    val isScheduleLoading: StateFlow<Boolean> = _isScheduleLoading.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow("全部")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    
    private val _showCurrentMonthOnly = MutableStateFlow(false)
    val showCurrentMonthOnly: StateFlow<Boolean> = _showCurrentMonthOnly.asStateFlow()
    
    private val _processedSchedule = MutableStateFlow<ProcessedSchedule?>(null)
    val processedSchedule: StateFlow<ProcessedSchedule?> = _processedSchedule.asStateFlow()
    
    init {
        loadApiConfig()
        loadData()
        startDateUpdater()
    }
    
    private fun startDateUpdater() {
        viewModelScope.launch {
            while (isActive) {
                val now = LocalDate.now()
                val tomorrow = now.plusDays(1)
                val millisUntilTomorrow = java.time.Duration.between(now.atStartOfDay(), tomorrow.atStartOfDay()).toMillis()
                delay(millisUntilTomorrow)
                _currentDate.value = LocalDate.now()
            }
        }
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
        updateScheduleCache()
    }
    
    private fun updateScheduleCache() {
        val baby = _baby.value ?: return
        val schedules = _schedules.value
        val currentDate = _currentDate.value
        
        val grouped = schedules.groupBy { record ->
            getVaccineMonthGroup(record.scheduledDate, baby.birthDate)
        }.toSortedMap(compareBy<Pair<String, java.time.YearMonth?>>({ it.second != null }).thenBy { it.second ?: java.time.YearMonth.of(1970, 1) })
        
        _scheduleCache.value = ScheduleCache(
            groupedSchedules = grouped,
            completedCount = schedules.count { it.isCompleted },
            totalCount = schedules.size,
            computedDate = currentDate
        )
        
        processScheduleData()
    }
    
    private fun processScheduleData() {
        val baby = _baby.value ?: return
        val cache = _scheduleCache.value ?: return
        val currentDate = _currentDate.value
        val filter = _selectedFilter.value
        val showCurrentMonthOnly = _showCurrentMonthOnly.value
        val selectedPaidVaccines = _selectedPaidVaccines.value
        
        val babyAgeInDays = ChronoUnit.DAYS.between(baby.birthDate, currentDate).toInt()
        val babyCurrentMonthIndex = babyAgeInDays / 30
        
        val overdueSchedules = cache.groupedSchedules.values.flatten().filter { 
            !it.isCompleted && isOverdue(it.scheduledDate, currentDate, baby.birthDate) 
        }
        
        val currentMonthVaccineSchedules = cache.groupedSchedules.values.flatten().filter { record ->
            val daysDiff = ChronoUnit.DAYS.between(baby.birthDate, record.scheduledDate).toInt()
            val vaccineMonthIndex = if (daysDiff < 30) -1 else daysDiff / 30
            vaccineMonthIndex == babyCurrentMonthIndex
        }
        
        val currentMonthWithOverdueSchedules = (currentMonthVaccineSchedules + overdueSchedules).distinctBy { it.id }
        
        val allRecords = cache.groupedSchedules.values.flatten()
        
        val filteredRecords = when (filter) {
            "全部" -> if (showCurrentMonthOnly) currentMonthWithOverdueSchedules else allRecords
            "待接种" -> (if (showCurrentMonthOnly) currentMonthWithOverdueSchedules else allRecords).filter { !it.isCompleted }
            "已完成" -> allRecords.filter { it.isCompleted }
            "免费" -> allRecords.filter { it.vaccine.isFree }
            "自费" -> allRecords.filter { !it.vaccine.isFree }
            else -> allRecords
        }
        
        val processedGrouped = filteredRecords
            .groupBy { getVaccineMonthGroup(it.scheduledDate, baby.birthDate) }
            .toSortedMap(compareBy<Pair<String, YearMonth?>>({ it.second != null }).thenBy { it.second ?: YearMonth.of(1970, 1) })
            .mapValues { (_, records) ->
                records.sortedByDescending { it.isCompleted }
            }
            .toSortedMap(compareBy<Pair<String, YearMonth?>>({ it.second != null }).thenBy { it.second ?: YearMonth.of(1970, 1) })
        
        val sortedRecordsByMonth = processedGrouped.mapValues { (_, records) ->
            val hasOverdue = records.any { !it.isCompleted && isOverdue(it.scheduledDate, currentDate, baby.birthDate) }
            Pair(records, hasOverdue)
        }
        
        _processedSchedule.value = ProcessedSchedule(
            groupedSchedules = processedGrouped,
            sortedRecordsByMonth = sortedRecordsByMonth,
            completedCount = cache.completedCount,
            totalCount = cache.totalCount,
            totalPaidPrice = selectedPaidVaccines.sumOf { it.price * it.doses }
        )
    }
    
    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        processScheduleData()
    }
    
    fun setShowCurrentMonthOnly(show: Boolean) {
        _showCurrentMonthOnly.value = show
        processScheduleData()
    }
    
    private fun getVaccineMonthGroup(scheduledDate: LocalDate, birthDate: LocalDate): Pair<String, java.time.YearMonth?> {
        val daysDiff = ChronoUnit.DAYS.between(birthDate, scheduledDate).toInt()
        
        if (daysDiff < 30) {
            return Pair("出生时", null)
        }
        
        val monthIndex = daysDiff / 30
        val month = java.time.YearMonth.from(birthDate).plusMonths(monthIndex.toLong())
        return Pair("", month)
    }
    
    fun markVaccinationCompleted(recordId: String) {
        _schedules.value = _schedules.value.map { record ->
            if (record.id == recordId) {
                record.copy(isCompleted = true, completedDate = LocalDate.now())
            } else {
                record
            }
        }
        updateScheduleCache()
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
        updateScheduleCache()
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
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = screen
    }
    
    fun navigateFromDetail(toScreen: Screen) {
        _previousScreen.value = _currentScreen.value
        _selectedVaccineForDetail.value = null
        _detailSourceScreen.value = null
        _currentScreen.value = toScreen
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
        _detailSourceScreen.value = fromScreen
        _previousScreen.value = fromScreen
        _selectedVaccineForDetail.value = vaccine
        _currentScreen.value = Screen.VaccineDetail
    }
    
    fun getFreeVaccines(): List<Vaccine> = VaccineRepository.freeVaccines
    
    fun getPaidVaccines(): List<Vaccine> = VaccineRepository.paidVaccines
    
    fun getVaccineById(id: String): Vaccine? = VaccineRepository.getVaccineById(id)
    
    fun setDeepseekApiKey(apiKey: String) {
        _deepseekApiKey.value = apiKey
        saveApiConfig()
    }
    
    fun getVaccinationScheduleString(): String {
        return _schedules.value
            .sortedBy { it.scheduledDate }
            .groupBy { 
                val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(_baby.value!!.birthDate, it.scheduledDate).toInt()
                if (daysDiff < 30) "出生时" else "${daysDiff / 30}月龄"
            }
            .map { (age, records) ->
                val vaccines = records.joinToString("\n") { 
                    "  - ${it.vaccine.chineseName} (第${it.doseNumber}剂, ${it.vaccine.name})" 
                }
                "$age:\n$vaccines"
            }
            .joinToString("\n")
    }
    
    fun getCurrentMonthVaccinesString(): String {
        val baby = _baby.value ?: return ""
        val today = LocalDate.now()
        val babyAgeInDays = java.time.temporal.ChronoUnit.DAYS.between(baby.birthDate, today).toInt()
        val babyCurrentMonthIndex = babyAgeInDays / 30
        
        val currentMonthVaccines = _schedules.value.filter { record ->
            val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(baby.birthDate, record.scheduledDate).toInt()
            val vaccineMonthIndex = if (daysDiff < 30) -1 else daysDiff / 30
            vaccineMonthIndex == babyCurrentMonthIndex
        }
        
        return if (currentMonthVaccines.isEmpty()) {
            "本月无需要接种的疫苗"
        } else {
            currentMonthVaccines.joinToString("\n") { 
                "  - ${it.vaccine.chineseName} (第${it.doseNumber}剂, 预计日期: ${it.scheduledDate})" 
            }
        }
    }
    
    fun getOverdueVaccinesString(): String {
        val baby = _baby.value ?: return ""
        val today = LocalDate.now()
        
        val overdueVaccines = _schedules.value.filter { 
            !it.isCompleted && isOverdue(it.scheduledDate, today, baby.birthDate) 
        }
        
        return if (overdueVaccines.isEmpty()) {
            "无逾期疫苗"
        } else {
            overdueVaccines.joinToString("\n") { 
                "  - ${it.vaccine.chineseName} (第${it.doseNumber}剂, 原定日期: ${it.scheduledDate})" 
            }
        }
    }
    
    fun getCurrentMonthTimeRange(): String {
        val baby = _baby.value ?: return ""
        val babyAgeInDays = java.time.temporal.ChronoUnit.DAYS.between(baby.birthDate, LocalDate.now()).toInt()
        val babyCurrentMonthIndex = babyAgeInDays / 30
        
        val startDate = baby.birthDate.plusDays((babyCurrentMonthIndex * 30).toLong())
        val endDate = baby.birthDate.plusDays(((babyCurrentMonthIndex + 1) * 30).toLong())
        
        return "${startDate} - ${endDate}"
    }
    
    suspend fun performOverallAnalysis(regenerate: Boolean = false): Boolean {
        if (!regenerate && _lastOverallAnalysisResult.value != null) {
            return true
        }
        
        val apiKey = _deepseekApiKey.value
        if (apiKey.isEmpty()) {
            _analysisError.value = "请先配置DeepSeek API Key"
            return false
        }
        
        _isAnalyzing.value = true
        _analysisError.value = null
        _streamingContent.value = ""
        
        try {
            val result = com.vaccineplanner.data.service.DeepSeekService.analyzeOverallPlan(
                apiKey = apiKey,
                birthDate = _baby.value?.birthDate?.toString() ?: "",
                schedule = getVaccinationScheduleString(),
                onContentUpdate = { content ->
                    _streamingContent.value = content
                }
            )
            
            if (result.success) {
                _lastOverallAnalysisResult.value = result.content
                saveAnalysisResults()
                return true
            } else {
                _analysisError.value = result.error
                return false
            }
        } catch (e: Exception) {
            _analysisError.value = e.message ?: "分析失败"
            return false
        } finally {
            _isAnalyzing.value = false
            _streamingContent.value = null
        }
    }
    
    suspend fun performCurrentMonthAnalysis(regenerate: Boolean = false): Boolean {
        if (!regenerate && _lastCurrentMonthAnalysisResult.value != null) {
            return true
        }
        
        val apiKey = _deepseekApiKey.value
        if (apiKey.isEmpty()) {
            _analysisError.value = "请先配置DeepSeek API Key"
            return false
        }
        
        _isAnalyzing.value = true
        _analysisError.value = null
        _streamingContent.value = ""
        
        try {
            val result = com.vaccineplanner.data.service.DeepSeekService.analyzeCurrentMonth(
                apiKey = apiKey,
                currentMonthVaccines = getCurrentMonthVaccinesString(),
                overdueVaccines = getOverdueVaccinesString(),
                timeRange = getCurrentMonthTimeRange(),
                onContentUpdate = { content ->
                    _streamingContent.value = content
                }
            )
            
            if (result.success) {
                _lastCurrentMonthAnalysisResult.value = result.content
                saveAnalysisResults()
                return true
            } else {
                _analysisError.value = result.error
                return false
            }
        } catch (e: Exception) {
            _analysisError.value = e.message ?: "分析失败"
            return false
        } finally {
            _isAnalyzing.value = false
            _streamingContent.value = null
        }
    }
    
    suspend fun performVaccineInfoQuery(vaccine: Vaccine): String? {
        val apiKey = _deepseekApiKey.value
        if (apiKey.isEmpty()) {
            _analysisError.value = "请先配置DeepSeek API Key"
            return null
        }
        
        _isAnalyzing.value = true
        _analysisError.value = null
        _streamingContent.value = ""
        
        try {
            val result = com.vaccineplanner.data.service.DeepSeekService.queryVaccineInfo(
                apiKey = apiKey,
                vaccineName = vaccine.chineseName,
                vaccineInfo = """
                    疫苗名称：${vaccine.chineseName} (${vaccine.name})
                    描述：${vaccine.description}
                    预防疾病：${vaccine.diseaseInfo.name}
                    疾病描述：${vaccine.diseaseInfo.description}
                    是否免费：${if (vaccine.isFree) "是" else "否"}
                    价格：${vaccine.price}元
                    剂次：${vaccine.doses}剂
                    ${vaccine.diseaseInfo.comparisonWithFree?.let { "与免费疫苗对比：$it" } ?: ""}
                """.trimIndent(),
                onContentUpdate = { content ->
                    _streamingContent.value = content
                }
            )
            
            if (result.success) {
                _lastVaccineInfoResult.value = result.content
                saveAnalysisResults()
                return result.content
            } else {
                _analysisError.value = result.error
                return null
            }
        } catch (e: Exception) {
            _analysisError.value = e.message ?: "查询失败"
            return null
        } finally {
            _isAnalyzing.value = false
            _streamingContent.value = null
        }
    }
    
    fun clearAnalysisError() {
        _analysisError.value = null
    }
    
    private fun saveApiConfig() {
        viewModelScope.launch {
            try {
                val configFile = File(getApplication<Application>().filesDir, "deepseek_config.json")
                val config = mapOf("apiKey" to _deepseekApiKey.value)
                configFile.writeText(gson.toJson(config))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadApiConfig() {
        viewModelScope.launch {
            try {
                val configFile = File(getApplication<Application>().filesDir, "deepseek_config.json")
                if (configFile.exists()) {
                    val json = configFile.readText()
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    val config: Map<String, String> = gson.fromJson(json, type)
                    _deepseekApiKey.value = config["apiKey"] ?: ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun saveAnalysisResults() {
        viewModelScope.launch {
            try {
                val configFile = File(getApplication<Application>().filesDir, "analysis_results.json")
                val results = mapOf(
                    "overall" to _lastOverallAnalysisResult.value,
                    "currentMonth" to _lastCurrentMonthAnalysisResult.value,
                    "vaccineInfo" to _lastVaccineInfoResult.value
                )
                configFile.writeText(gson.toJson(results))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadAnalysisResults() {
        viewModelScope.launch {
            try {
                val configFile = File(getApplication<Application>().filesDir, "analysis_results.json")
                if (configFile.exists()) {
                    val json = configFile.readText()
                    val type = object : TypeToken<Map<String, String?>>() {}.type
                    val results: Map<String, String?> = gson.fromJson(json, type)
                    _lastOverallAnalysisResult.value = results["overall"]
                    _lastCurrentMonthAnalysisResult.value = results["currentMonth"]
                    _lastVaccineInfoResult.value = results["vaccineInfo"]
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun isOverdue(scheduledDate: LocalDate, today: LocalDate, birthDate: LocalDate): Boolean {
        val daysSinceBirth = java.time.temporal.ChronoUnit.DAYS.between(birthDate, scheduledDate).toInt()
        val monthIndex = daysSinceBirth / 30
        val deadline = birthDate.plusDays(((monthIndex + 1) * 30).toLong())
        return today.isAfter(deadline)
    }
    
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
                    updateScheduleCache()
                    delay(300)
                }
                loadAnalysisResults()
                _isScheduleLoading.value = false
            } catch (e: Exception) {
                e.printStackTrace()
                _isScheduleLoading.value = false
            }
        }
    }
    
    fun resetAll() {
        _baby.value = null
        _schedules.value = emptyList()
        _selectedPaidVaccines.value = emptyList()
        _currentScreen.value = Screen.BabyInfo
        _lastOverallAnalysisResult.value = null
        _lastCurrentMonthAnalysisResult.value = null
        _lastVaccineInfoResult.value = null
        _scheduleCache.value = null
        _isScheduleLoading.value = true
        prefsFile.delete()
        val configFile = File(getApplication<Application>().filesDir, "analysis_results.json")
        if (configFile.exists()) {
            configFile.delete()
        }
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
    object AISettings : Screen(4)
    object AIAnalysisResult : Screen(5)
    object DonateScreen : Screen(6)
}

data class ScheduleCache(
    val groupedSchedules: java.util.SortedMap<Pair<String, java.time.YearMonth?>, List<VaccinationRecord>>,
    val completedCount: Int,
    val totalCount: Int,
    val computedDate: LocalDate
)

data class ProcessedSchedule(
    val groupedSchedules: java.util.SortedMap<Pair<String, YearMonth?>, List<VaccinationRecord>>,
    val sortedRecordsByMonth: Map<Pair<String, YearMonth?>, Pair<List<VaccinationRecord>, Boolean>>,
    val completedCount: Int,
    val totalCount: Int,
    val totalPaidPrice: Double
)
