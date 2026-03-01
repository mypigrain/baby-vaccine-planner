package com.vaccineplanner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaccineplanner.data.model.DiseaseInfo
import com.vaccineplanner.data.model.EpidemicData
import com.vaccineplanner.data.model.Vaccine
import com.vaccineplanner.data.model.VaccineCategory
import com.vaccineplanner.ui.theme.PaidVaccineOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineDetailScreen(
    vaccine: Vaccine,
    isSelected: Boolean,
    onAddToSchedule: () -> Unit,
    onRemoveFromSchedule: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vaccine.chineseName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            if (!vaccine.isFree) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "参考价格",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            val totalPrice = vaccine.price * vaccine.doses
                            Text(
                                text = "¥${vaccine.price}/剂 x ${vaccine.doses}剂 = ¥${totalPrice.toInt()}元",
                                style = MaterialTheme.typography.titleMedium,
                                color = PaidVaccineOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (isSelected) {
                            OutlinedButton(
                                onClick = onRemoveFromSchedule,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.RemoveCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("从计划中移除")
                            }
                        } else {
                            Button(
                                onClick = onAddToSchedule,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PaidVaccineOrange
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("加入接种计划")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                VaccineHeader(vaccine)
            }
            
            item {
                DiseaseInfoSection(vaccine.diseaseInfo)
            }
            
            if (vaccine.diseaseInfo.comparisonWithFree != null) {
                item {
                    ComparisonWithFreeSection(vaccine.diseaseInfo.comparisonWithFree)
                }
            }
            
            if (vaccine.replaceableVaccines.isNotEmpty()) {
                item {
                    ReplaceableSection(vaccine)
                }
            }
            
            item {
                VaccinationInfoSection(vaccine)
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun VaccineHeader(vaccine: Vaccine) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (vaccine.isFree) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = vaccine.chineseName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (vaccine.isFree) "免费疫苗" else "自费疫苗",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (vaccine.isFree) Color(0xFF4CAF50) else PaidVaccineOrange,
                    modifier = Modifier
                        .background(
                            if (vaccine.isFree) Color(0xFF4CAF50).copy(alpha = 0.1f) else PaidVaccineOrange.copy(alpha = 0.1f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = vaccine.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            
            if (vaccine.category == VaccineCategory.REPLACEMENT) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.SwapCalls,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "可替换对应免费疫苗",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun DiseaseInfoSection(diseaseInfo: DiseaseInfo) {
    var expandedEpidemic by remember { mutableStateOf<String?>(null) }
    
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "预防疾病: ${diseaseInfo.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = diseaseInfo.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            if (diseaseInfo.incidenceData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "疾病数据",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                diseaseInfo.incidenceData.forEach { data ->
                    EpidemicDataItem(
                        data = data,
                        isExpanded = expandedEpidemic == data.title,
                        onToggle = {
                            expandedEpidemic = if (expandedEpidemic == data.title) null else data.title
                        }
                    )
                }
            }

            if (diseaseInfo.outcomes != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "患病后的可能情况",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutcomeItem("最好情况", diseaseInfo.outcomes.best, Color(0xFF4CAF50))
                OutcomeItem("最差情况", diseaseInfo.outcomes.worst, Color(0xFFF44336))
                OutcomeItem("常见情况", diseaseInfo.outcomes.typical, Color(0xFFFF9800))
            }
        }
    }
}

@Composable
private fun EpidemicDataItem(
    data: EpidemicData,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = data.value,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (data.details.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "数据来源: ${data.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                data.details.forEach { detail ->
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        Text("• ", color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "${detail.title}: ${detail.value}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun OutcomeItem(title: String, content: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(4.dp))
                .padding(top = 6.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ComparisonWithFreeSection(comparison: String) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CompareArrows, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "与免费疫苗的区别",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = comparison,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ReplaceableSection(vaccine: Vaccine) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "可替换的免费疫苗",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "接种此疫苗后，无需再接种以下免费疫苗:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            vaccine.replaceableVaccines.forEach { freeId ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.RemoveCircleOutline,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getVaccineChineseName(freeId),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun VaccinationInfoSection(vaccine: Vaccine) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "接种信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow("接种剂次", "${vaccine.doses}剂")
            InfoRow("推荐年龄", vaccine.ageRange)
            
            if (vaccine.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "注意事项: ${vaccine.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getVaccineChineseName(id: String): String {
    return when (id) {
        "dtp" -> "百白破疫苗"
        "polio" -> "脊髓灰质炎疫苗"
        "hib" -> "b型流感嗜血杆菌疫苗"
        "bcg" -> "卡介苗"
        "hepb" -> "乙肝疫苗"
        "measles" -> "麻腮风疫苗"
        else -> id
    }
}
