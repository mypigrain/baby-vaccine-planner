package com.vaccineplanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vaccineplanner.data.model.Vaccine
import com.vaccineplanner.ui.components.VaccineInfoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaidVaccineListScreen(
    paidVaccines: List<Vaccine>,
    selectedVaccines: List<Vaccine>,
    onVaccineSelect: (Vaccine) -> Unit,
    onVaccineDeselect: (Vaccine) -> Unit,
    onViewDetail: (Vaccine) -> Unit,
    onNavigateBack: () -> Unit,
    savedScrollPosition: Int = 0,
    onSaveScrollPosition: (Int) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val listState = rememberLazyListState()
    
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
    
    val filteredVaccines = paidVaccines.filter { 
        searchQuery.isBlank() || it.chineseName.contains(searchQuery) || it.name.contains(searchQuery)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自费疫苗选择") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索疫苗") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            if (selectedVaccines.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "已选择 ${selectedVaccines.size} 种自费疫苗",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "已选择的疫苗将自动添加到接种计划表中",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val replacementVaccines = filteredVaccines.filter { it.category == com.vaccineplanner.data.model.VaccineCategory.REPLACEMENT }
                val voluntaryVaccines = filteredVaccines.filter { it.category == com.vaccineplanner.data.model.VaccineCategory.VOLUNTARY }
                
                if (replacementVaccines.isNotEmpty()) {
                    item {
                        Text(
                            text = "可替换免费疫苗",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    item {
                        Text(
                            text = "这些疫苗可以替代相应的免费疫苗，接种后可不再接种对应的免费疫苗",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(replacementVaccines, key = { it.id }) { vaccine ->
                        VaccineInfoCard(
                            vaccine = vaccine,
                            isSelected = selectedVaccines.any { it.id == vaccine.id },
                            onSelect = {
                                if (selectedVaccines.any { it.id == vaccine.id }) {
                                    onVaccineDeselect(vaccine)
                                } else {
                                    onVaccineSelect(vaccine)
                                }
                            },
                            onDetailClick = { onViewDetail(vaccine) },
                            showPaidBadge = false
                        )
                    }
                }
                
                if (voluntaryVaccines.isNotEmpty()) {
                    item {
                        Text(
                            text = "额外自费疫苗",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    item {
                        Text(
                            text = "这些疫苗是免费疫苗的补充，可根据需求选择接种",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(voluntaryVaccines, key = { it.id }) { vaccine ->
                        VaccineInfoCard(
                            vaccine = vaccine,
                            isSelected = selectedVaccines.any { it.id == vaccine.id },
                            onSelect = {
                                if (selectedVaccines.any { it.id == vaccine.id }) {
                                    onVaccineDeselect(vaccine)
                                } else {
                                    onVaccineSelect(vaccine)
                                }
                            },
                            onDetailClick = { onViewDetail(vaccine) },
                            showPaidBadge = false
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
