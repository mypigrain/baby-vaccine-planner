package com.vaccineplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BabyInfoScreen(
    onSubmit: (String, LocalDate) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "疫苗接种计划",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "请输入宝宝信息",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { 
                name = it
                nameError = false
            },
            label = { Text("宝宝姓名") },
            placeholder = { Text("请输入宝宝姓名") },
            isError = nameError,
            supportingText = if (nameError) {{ Text("请输入宝宝姓名") }} else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = birthDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "",
            onValueChange = { },
            label = { Text("出生日期") },
            placeholder = { Text("请选择宝宝出生日期") },
            readOnly = true,
            isError = dateError,
            supportingText = if (dateError) {{ Text("请选择宝宝出生日期") }} else null,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                nameError = name.isBlank()
                dateError = birthDate == null
                
                if (!nameError && !dateError && birthDate != null) {
                    onSubmit(name, birthDate!!)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("生成接种计划", style = MaterialTheme.typography.titleMedium)
        }
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            birthDate = LocalDate.ofEpochDay(it / (24 * 60 * 60 * 1000))
                            dateError = false
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
