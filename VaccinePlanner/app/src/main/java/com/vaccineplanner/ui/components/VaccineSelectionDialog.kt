package com.vaccineplanner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaccineplanner.data.model.Vaccine

@Composable
fun VaccineSelectionDialog(
    vaccines: List<Vaccine>,
    onDismiss: () -> Unit,
    onVaccineSelected: (Vaccine) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    
    val filteredVaccines = vaccines.filter {
        it.chineseName.contains(searchText, ignoreCase = true) ||
        it.name.contains(searchText, ignoreCase = true)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择要查询的疫苗") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("搜索疫苗") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredVaccines) { vaccine ->
                        VaccineSelectionItem(
                            vaccine = vaccine,
                            onClick = {
                                onVaccineSelected(vaccine)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun VaccineSelectionItem(
    vaccine: Vaccine,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vaccine.chineseName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = vaccine.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!vaccine.isFree) {
                    Text(
                        text = "¥${vaccine.price}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(
                text = if (vaccine.isFree) "免费" else "自费",
                style = MaterialTheme.typography.labelSmall,
                color = if (vaccine.isFree) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}