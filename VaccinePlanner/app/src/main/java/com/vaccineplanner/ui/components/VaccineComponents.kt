package com.vaccineplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.vaccineplanner.data.model.VaccinationRecord
import com.vaccineplanner.data.model.Vaccine
import com.vaccineplanner.data.model.VaccineType
import com.vaccineplanner.ui.theme.FreeVaccineGreen
import com.vaccineplanner.ui.theme.PaidVaccineOrange
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun VaccinationCard(
    record: VaccinationRecord,
    babyBirthDate: LocalDate,
    onMarkCompleted: (String) -> Unit,
    onMarkIncomplete: (String) -> Unit,
    onClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val vaccine = record.vaccine
    val isPaid = !vaccine.isFree
    val today = LocalDate.now()
    
    val daysDiff = ChronoUnit.DAYS.between(babyBirthDate, record.scheduledDate).toInt()
    val monthIndex = daysDiff / 30
    val monthGroup = YearMonth.from(babyBirthDate).plusMonths(monthIndex.toLong())
    val monthEndDate = monthGroup.atEndOfMonth()
    val daysSinceMonthEnd = ChronoUnit.DAYS.between(monthEndDate, today).toInt()
    val isOverdue = !record.isCompleted && daysSinceMonthEnd > 30
    
    val cardColor = when {
        record.isCompleted -> Color(0xFFE8F5E9)
        isOverdue -> Color(0xFFFFEBEE)
        isPaid -> Color(0xFFFFF3E0)
        else -> Color.White
    }
    
    val typeColor = if (isPaid) PaidVaccineOrange else FreeVaccineGreen
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (record.isCompleted) Icons.Default.CheckCircle else Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (record.isCompleted) Color(0xFF4CAF50) else typeColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = vaccine.chineseName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "查看疫苗信息",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "第${record.doseNumber}剂",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isPaid) {
                        Text(
                            text = "自费",
                            fontSize = 10.sp,
                            color = PaidVaccineOrange,
                            modifier = Modifier
                                .background(PaidVaccineOrange.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    } else {
                        Text(
                            text = "免费",
                            fontSize = 10.sp,
                            color = FreeVaccineGreen,
                            modifier = Modifier
                                .background(FreeVaccineGreen.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val daysSinceBirth = ChronoUnit.DAYS.between(babyBirthDate, record.scheduledDate).toInt()
                val monthIndex = daysSinceBirth / 30
                val monthStart = babyBirthDate.plusMonths(monthIndex.toLong())
                val monthEnd = babyBirthDate.plusMonths((monthIndex + 1).toLong()).minusDays(1)
                val dateRangeText = "${monthStart.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))} - ${monthEnd.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))}"
                
                Text(
                    text = dateRangeText,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                
                if (record.isCompleted && record.completedDate != null) {
                    Text(
                        text = "已完成接种",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                } else if (isOverdue) {
                    Text(
                        text = "已逾期",
                        fontSize = 12.sp,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (record.isCompleted) {
                IconButton(onClick = { onMarkIncomplete(record.id) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "取消已完成",
                        tint = Color.Gray
                    )
                }
            } else {
                IconButton(onClick = { onMarkCompleted(record.id) }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "标记已完成",
                        tint = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun VaccineInfoCard(
    vaccine: Vaccine,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDetailClick: (() -> Unit)? = null,
    showPaidBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    val typeColor = if (vaccine.isFree) FreeVaccineGreen else PaidVaccineOrange
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) typeColor.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (vaccine.isFree) Icons.Default.CheckCircle else Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = vaccine.chineseName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (showPaidBadge) {
                        Text(
                            text = "自费",
                            fontSize = 10.sp,
                            color = typeColor,
                            modifier = Modifier
                                .background(typeColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = vaccine.description,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                
                if (!vaccine.isFree) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val totalPrice = vaccine.price * vaccine.doses
                    Text(
                        text = if (vaccine.doses > 1) "参考价格: ¥${vaccine.price}/剂 x ${vaccine.doses}剂 = ¥${totalPrice.toInt()}元" else "参考价格: ¥${vaccine.price}/剂",
                        fontSize = 12.sp,
                        color = PaidVaccineOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (!vaccine.isFree) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (onDetailClick != null) {
                        OutlinedButton(
                            onClick = onDetailClick,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("介绍", fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelect() },
                        colors = CheckboxDefaults.colors(checkedColor = PaidVaccineOrange)
                    )
                }
            }
        }
    }
}

private fun calculateAge(birthDate: LocalDate, targetDate: LocalDate): String {
    val days = ChronoUnit.DAYS.between(birthDate, targetDate).toInt()
    val monthIndex = days / 30
    
    return when {
        days < 30 -> "出生时"
        monthIndex == 1 -> "1月龄"
        monthIndex <= 12 -> "${monthIndex}月龄"
        else -> "${monthIndex / 12}岁${monthIndex % 12}月龄"
    }
}
