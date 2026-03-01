package com.vaccineplanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaccineplanner.data.model.Baby
import com.vaccineplanner.data.model.VaccinationRecord
import com.vaccineplanner.data.model.Vaccine
import com.vaccineplanner.ui.components.AIAnalysisTypeDialog
import com.vaccineplanner.ui.components.VaccinationCard
import com.vaccineplanner.ui.theme.FreeVaccineGreen
import com.vaccineplanner.ui.theme.PaidVaccineOrange
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineScheduleScreen(
    baby: Baby,
    schedules: List<VaccinationRecord>,
    selectedPaidVaccines: List<Vaccine>,
    onMarkCompleted: (String) -> Unit,
    onMarkIncomplete: (String) -> Unit,
    onNavigateToPaidVaccines: () -> Unit,
    onNavigateToVaccineDetail: (Vaccine) -> Unit,
    onAIOverallAnalysis: () -> Unit,
    onAICurrentMonthAnalysis: () -> Unit,
    onAIVaccineInfoQuery: () -> Unit,
    onNavigateToAISettings: () -> Unit,
    onNavigateToDonate: () -> Unit,
    onReset: () -> Unit,
    savedScrollPosition: Int = 0,
    onSaveScrollPosition: (Int) -> Unit = {}
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("全部") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showCurrentMonthOnly by remember { mutableStateOf(false) }
    var showAIAnalysisMenu by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(savedScrollPosition) {
        if (savedScrollPosition > 0) {
            listState.scrollToItem(savedScrollPosition)
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            onSaveScrollPosition(listState.firstVisibleItemIndex)
        }
    }
    
    val totalPaidPrice = selectedPaidVaccines.sumOf { it.price * it.doses }
    val today = LocalDate.now()
    val babyAgeInDays = ChronoUnit.DAYS.between(baby.birthDate, today).toInt()
    val babyCurrentMonthIndex = babyAgeInDays / 30
    
    val overdueSchedules = schedules.filter { 
        !it.isCompleted && isOverdue(it.scheduledDate, today, baby.birthDate) 
    }
    
    val currentMonthVaccineSchedules = schedules.filter { record ->
        val daysDiff = ChronoUnit.DAYS.between(baby.birthDate, record.scheduledDate).toInt()
        val vaccineMonthIndex = if (daysDiff < 30) -1 else daysDiff / 30
        vaccineMonthIndex == babyCurrentMonthIndex
    }
    
    val currentMonthWithOverdueSchedules = (currentMonthVaccineSchedules + overdueSchedules).distinctBy { it.id }
    
    val filteredSchedules = when (selectedFilter) {
        "全部" -> if (showCurrentMonthOnly) currentMonthWithOverdueSchedules else schedules
        "待接种" -> (if (showCurrentMonthOnly) currentMonthWithOverdueSchedules else schedules).filter { !it.isCompleted }
        "已完成" -> schedules.filter { it.isCompleted }
        "免费" -> schedules.filter { it.vaccine.isFree }
        "自费" -> schedules.filter { !it.vaccine.isFree }
        else -> schedules
    }
    
    val groupedSchedules = filteredSchedules.groupBy { record ->
        getVaccineMonthGroup(record.scheduledDate, baby.birthDate)
    }.toSortedMap(compareBy<Pair<String, YearMonth?>>({ it.second != null }).thenBy { it.second ?: YearMonth.of(1970, 1) })
    
    val completedCount = schedules.count { it.isCompleted }
    val totalCount = schedules.size
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "${baby.name}的接种计划",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    TextButton(
                        onClick = onNavigateToDonate,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "捐赠",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "捐赠",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { showAIAnalysisMenu = true },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "AI分析",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "AI分析",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToPaidVaccines,
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)) },
                text = { Text("添加自费疫苗", fontSize = 13.sp) },
                containerColor = PaidVaccineOrange,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.height(40.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(
                    selected = showCurrentMonthOnly,
                    onClick = { showCurrentMonthOnly = !showCurrentMonthOnly },
                    label = { Text(if (showCurrentMonthOnly) "仅当月 ✓" else "仅当月") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    FilterChip(
                        selected = selectedFilter != "全部",
                        onClick = { showFilterMenu = true },
                        label = { Text(selectedFilter) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        listOf("全部", "待接种", "已完成", "免费", "自费").forEach { filter ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(filter)
                                        if (selectedFilter == filter) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    selectedFilter = filter
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
                
                FilterChip(
                    selected = false,
                    onClick = { showResetDialog = true },
                    label = { Text("重置") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "已完成",
                    value = "$completedCount",
                    subtitle = "/ $totalCount 剂",
                    color = FreeVaccineGreen
                )
                StatCard(
                    title = "待接种",
                    value = "${totalCount - completedCount}",
                    subtitle = "剂",
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    title = "自费费用",
                    value = "¥${totalPaidPrice}",
                    subtitle = "元",
                    color = PaidVaccineOrange
                )
            }
            
            if (showCurrentMonthOnly) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "仅显示当月及逾期疫苗",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Divider()
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                groupedSchedules.forEach { (monthGroup, records) ->
                    val monthLabel = monthGroup.first
                    val month = monthGroup.second
                    val monthIndex = if (monthLabel == "出生时") {
                        -1
                    } else if (month != null) {
                        ChronoUnit.MONTHS.between(baby.birthDate.withDayOfMonth(1), month.atDay(1)).toInt()
                    } else 0
                    val isCurrentOrPast = monthIndex <= babyCurrentMonthIndex
                    val isOverdueMonth = records.any { !it.isCompleted && isOverdue(it.scheduledDate, today, baby.birthDate) }
                    val sortedRecords = records.sortedByDescending { it.isCompleted }
                    
                    item {
                        MonthHeader(
                            monthLabel = monthLabel,
                            monthIndex = monthIndex,
                            isCurrentOrPast = isCurrentOrPast,
                            hasOverdue = isOverdueMonth,
                            completedCount = records.count { it.isCompleted },
                            totalCount = records.size
                        )
                    }
                    
                    items(sortedRecords, key = { it.id }) { record ->
                        VaccinationCard(
                            record = record,
                            babyBirthDate = baby.birthDate,
                            onMarkCompleted = onMarkCompleted,
                            onMarkIncomplete = onMarkIncomplete,
                            onClick = { onNavigateToVaccineDetail(record.vaccine) },
                            onInfoClick = { onNavigateToVaccineDetail(record.vaccine) }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    if (showResetDialog) {
        ResetConfirmDialog(
            onConfirm = onReset,
            onDismiss = { showResetDialog = false }
        )
    }
    
    if (showAIAnalysisMenu) {
        AnimatedVisibility(
            visible = showAIAnalysisMenu,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            AIAnalysisTypeDialog(
                onDismiss = { showAIAnalysisMenu = false },
                onOverallAnalysis = {
                    showAIAnalysisMenu = false
                    onAIOverallAnalysis()
                },
                onCurrentMonthAnalysis = {
                    showAIAnalysisMenu = false
                    onAICurrentMonthAnalysis()
                },
                onVaccineInfoQuery = {
                    showAIAnalysisMenu = false
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(100) // Wait for exit animation
                        onAIVaccineInfoQuery()
                    }
                },
                onNavigateToSettings = {
                    showAIAnalysisMenu = false
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(100) // Wait for exit animation
                        onNavigateToAISettings()
                    }
                }
            )
        }
    }
}

private fun isOverdue(scheduledDate: LocalDate, today: LocalDate, birthDate: LocalDate): Boolean {
    val daysSinceBirth = ChronoUnit.DAYS.between(birthDate, scheduledDate).toInt()
    val monthIndex = daysSinceBirth / 30
    val deadline = birthDate.plusDays(((monthIndex + 1) * 30).toLong())
    return today.isAfter(deadline)
}

private fun getVaccineMonthGroup(scheduledDate: LocalDate, birthDate: LocalDate): Pair<String, YearMonth?> {
    val daysDiff = ChronoUnit.DAYS.between(birthDate, scheduledDate).toInt()
    
    if (daysDiff < 30) {
        return Pair("出生时", null)
    }
    
    val monthIndex = daysDiff / 30
    val month = YearMonth.from(birthDate).plusMonths(monthIndex.toLong())
    return Pair("", month)
}

@Composable
private fun MonthHeader(
    monthLabel: String,
    monthIndex: Int,
    isCurrentOrPast: Boolean,
    hasOverdue: Boolean,
    completedCount: Int,
    totalCount: Int
) {
    val ageText = when {
        monthLabel == "出生时" -> "出生时"
        monthIndex <= 0 -> "0月龄"
        monthIndex == 1 -> "1月龄"
        monthIndex <= 12 -> "${monthIndex}月龄"
        else -> "${monthIndex / 12}岁${monthIndex % 12}月龄"
    }
    
    val headerColor = when {
        hasOverdue -> MaterialTheme.colorScheme.errorContainer
        isCurrentOrPast -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when {
        hasOverdue -> MaterialTheme.colorScheme.onErrorContainer
        isCurrentOrPast -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerColor, MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasOverdue) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "有逾期",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = ageText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        Text(
            text = "$completedCount/$totalCount",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ResetConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认重置") },
        text = { Text("确定要重置所有接种记录吗？此操作将清除所有已登记的接种信息，且无法恢复。") },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("确认重置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
