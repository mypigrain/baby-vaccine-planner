package com.vaccineplanner.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
    
    val cardData = remember(record.id, record.isCompleted, record.scheduledDate, babyBirthDate) {
        val daysDiff = ChronoUnit.DAYS.between(babyBirthDate, record.scheduledDate).toInt()
        val monthIndex = daysDiff / 30
        val deadline = babyBirthDate.plusDays(((monthIndex + 1) * 30).toLong())
        val isOverdue = !record.isCompleted && LocalDate.now().isAfter(deadline)
        
        val color = when {
            record.isCompleted -> Color(0xFFE8F5E9)
            isOverdue -> Color(0xFFFFEBEE)
            isPaid -> Color(0xFFFFF3E0)
            else -> Color.White
        }
        
        Triple(color, isOverdue, monthIndex)
    }
    
    val cardColor = cardData.first
    val isOverdue = cardData.second
    val monthIndex = cardData.third
    val typeColor = if (isPaid) PaidVaccineOrange else FreeVaccineGreen
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(typeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = record.isCompleted,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(durationMillis = 200)) + 
                         scaleIn(animationSpec = tween(durationMillis = 200))) togetherWith
                        (fadeOut(animationSpec = tween(durationMillis = 200)) + 
                         scaleOut(animationSpec = tween(durationMillis = 200)))
                    },
                    label = "statusIcon"
                ) { isCompleted ->
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (isCompleted) Color(0xFF4CAF50) else typeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = vaccine.chineseName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    TextButton(
                        onClick = onInfoClick,
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                        modifier = Modifier.height(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        Text(
                            text = "介绍",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "第${record.doseNumber}剂",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isPaid) {
                        Text(
                            text = "自费",
                            fontSize = 9.sp,
                            color = PaidVaccineOrange,
                            modifier = Modifier
                                .background(PaidVaccineOrange.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    } else {
                        Text(
                            text = "免费",
                            fontSize = 9.sp,
                            color = FreeVaccineGreen,
                            modifier = Modifier
                                .background(FreeVaccineGreen.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                val dateRangeText = remember(monthIndex, babyBirthDate) {
                    val monthStart = babyBirthDate.plusMonths(monthIndex.toLong())
                    val monthEnd = babyBirthDate.plusMonths((monthIndex + 1).toLong()).minusDays(1)
                    "${monthStart.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))} - ${monthEnd.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))}"
                }
                
                Text(
                    text = dateRangeText,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                
                if (record.isCompleted && record.completedDate != null) {
                    Text(
                        text = "已完成",
                        fontSize = 10.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium
                    )
                } else if (isOverdue) {
                    Text(
                        text = "已逾期",
                        fontSize = 10.sp,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (record.isCompleted) {
                OutlinedButton(
                    onClick = { onMarkIncomplete(record.id) },
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Text("完成", fontSize = 11.sp)
                }
            } else {
                Button(
                    onClick = { onMarkCompleted(record.id) },
                    modifier = Modifier.height(28.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FreeVaccineGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("接种", fontSize = 11.sp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(width = 0.5.dp, color = Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(typeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (vaccine.isFree) Icons.Default.CheckCircle else Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
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
                    Spacer(modifier = Modifier.width(5.dp))
                    if (showPaidBadge) {
                        Text(
                            text = "自费",
                            fontSize = 10.sp,
                            color = typeColor,
                            modifier = Modifier
                                .background(typeColor.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(3.dp))
                
                Text(
                    text = vaccine.description,
                    fontSize = 12.sp,
                    lineHeight = (12 * 1.1).sp,
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
