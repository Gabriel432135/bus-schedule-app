/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.busschedule.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.busschedule.R
import com.example.busschedule.data.BusSchedule
import com.example.busschedule.ui.theme.AppTheme
import com.example.busschedule.ui.theme.BusScheduleTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BusScheduleScreens {
    FullSchedule,
    RouteSchedule
}

@Composable
fun BusScheduleApp(
    viewModel: BusScheduleViewModel = viewModel(factory = BusScheduleViewModel.factory)
) {
    val navController = rememberNavController()
    val fullScheduleTitle = stringResource(R.string.full_schedule)
    var topAppBarTitle by remember { mutableStateOf(fullScheduleTitle) }
    val fullSchedule by viewModel.getFullSchedule().collectAsState(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var scheduleToDelete by remember { mutableStateOf<BusSchedule?>(null) }

    val onBackHandler = {
        topAppBarTitle = fullScheduleTitle
        navController.navigateUp()
    }

    if (showAddDialog) {
        AddScheduleDialog(
            onDismissRequest = { showAddDialog = false },
            onConfirm = { stopName, arrivalTime ->
                // TODO: Chamar o ViewModel para salvar o novo horário no Room
                showAddDialog = false
            }
        )
    }

    scheduleToDelete?.let { schedule ->
        DeleteConfirmationDialog(
            onDismissRequest = { scheduleToDelete = null },
            onConfirm = {
                // TODO: Chamar o ViewModel para remover o item no Room
                scheduleToDelete = null
            }
        )
    }

    Scaffold(
        topBar = {
            BusScheduleTopAppBar(
                title = topAppBarTitle,
                canNavigateBack = navController.previousBackStackEntry != null,
                onBackClick = { onBackHandler() }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BusScheduleScreens.FullSchedule.name
        ) {
            composable(BusScheduleScreens.FullSchedule.name) {
                FullScheduleScreen(
                    busSchedules = fullSchedule,
                    contentPadding = innerPadding,
                    onScheduleClick = { busStopName ->
                        navController.navigate(
                            "${BusScheduleScreens.RouteSchedule.name}/$busStopName"
                        )
                        topAppBarTitle = busStopName
                    },
                    onAddClick = { showAddDialog = true },
                    onDeleteClick = { schedule ->
                        scheduleToDelete = schedule
                    }
                )
            }
            val busRouteArgument = "busRoute"
            composable(
                route = BusScheduleScreens.RouteSchedule.name + "/{$busRouteArgument}",
                arguments = listOf(navArgument(busRouteArgument) { type = NavType.StringType })
            ) { backStackEntry ->
                val stopName = backStackEntry.arguments?.getString(busRouteArgument)
                    ?: error("busRouteArgument cannot be null")
                val routeSchedule by viewModel.getScheduleFor(stopName).collectAsState(emptyList())
                RouteScheduleScreen(
                    stopName = stopName,
                    busSchedules = routeSchedule,
                    contentPadding = innerPadding,
                    onBack = { onBackHandler() }
                )
            }
        }
    }
}

@Composable
fun FullScheduleScreen(
    busSchedules: List<BusSchedule>,
    onScheduleClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onAddClick: (() -> Unit)? = null,
    onDeleteClick: ((BusSchedule) -> Unit)? = null
) {
    Box(modifier = modifier.fillMaxSize()) {
        BusScheduleScreen(
            busSchedules = busSchedules,
            onScheduleClick = onScheduleClick,
            onDeleteClick = onDeleteClick,
            contentPadding = contentPadding
        )
        if (onAddClick != null) {
            FloatingActionButton(
                onClick = onAddClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AppTheme.dimensions.paddingExtraLarge)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar Horário"
                )
            }
        }
    }
}

@Composable
fun RouteScheduleScreen(
    stopName: String,
    busSchedules: List<BusSchedule>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onBack: () -> Unit = {}
) {
    BackHandler { onBack() }
    BusScheduleScreen(
        busSchedules = busSchedules,
        modifier = modifier,
        contentPadding = contentPadding,
        stopName = stopName
    )
}

@Composable
fun BusScheduleScreen(
    busSchedules: List<BusSchedule>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    stopName: String? = null,
    onScheduleClick: ((String) -> Unit)? = null,
    onDeleteClick: ((BusSchedule) -> Unit)? = null
) {
    val stopNameText = if (stopName == null) {
        stringResource(R.string.stop_name)
    } else {
        "$stopName ${stringResource(R.string.route_stop_name)}"
    }
    val layoutDirection = LocalLayoutDirection.current
    Column(
        modifier = modifier.padding(
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection),
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = AppTheme.dimensions.paddingExtraLarge,
                    start = AppTheme.dimensions.paddingExtraLarge,
                    end = AppTheme.dimensions.paddingExtraLarge,
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stopNameText)
            Text(stringResource(R.string.arrival_time))
        }
        HorizontalDivider()
        BusScheduleDetails(
            contentPadding = PaddingValues(
                bottom = contentPadding.calculateBottomPadding()
            ),
            busSchedules = busSchedules,
            onScheduleClick = onScheduleClick,
            onDeleteClick = onDeleteClick
        )
    }
}

/*
 * Composable for BusScheduleDetails which show list of bus schedule
 * When [onScheduleClick] is null, [stopName] is replaced with placeholder
 * as it is assumed [stopName]s are the same as shown
 * in the list heading display in [BusScheduleScreen]
 */
@Composable
fun BusScheduleDetails(
    busSchedules: List<BusSchedule>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onScheduleClick: ((String) -> Unit)? = null,
    onDeleteClick: ((BusSchedule) -> Unit)? = null
) {
    val formatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        items(
            items = busSchedules,
            key = { busSchedule -> busSchedule.id }
        ) { schedule ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onScheduleClick != null) {
                        onScheduleClick?.invoke(schedule.stopName)
                    }
                    .padding(AppTheme.dimensions.paddingExtraLarge),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onScheduleClick == null) {
                    Text(
                        text = "--",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight(300)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = schedule.stopName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight(300)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = formatter.format(Date(schedule.arrivalTimeInMillis.toLong() * 1000)),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight(600)
                    ),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
                if (onDeleteClick != null) {
                    IconButton(onClick = { onDeleteClick(schedule) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddScheduleDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (stopName: String, arrivalTime: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var stopName by remember { mutableStateOf("") }
    var arrivalTime by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Adicionar Horário") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimensions.paddingLarge)
            ) {
                OutlinedTextField(
                    value = stopName,
                    onValueChange = { stopName = it },
                    label = { Text("Nome da Parada") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = arrivalTime,
                    onValueChange = { arrivalTime = it },
                    label = { Text("Horário de Chegada (ex: 08:30 AM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (stopName.isNotBlank() && arrivalTime.isNotBlank()) {
                        onConfirm(stopName, arrivalTime)
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        },
        modifier = modifier
    )
}

@Composable
fun DeleteConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Excluir Horário") },
        text = { Text(text = "Tem certeza de que deseja excluir este horário da parada?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(text = "Sim", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Cancelar")
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScheduleTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (canNavigateBack) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(
                            R.string.back
                        )
                    )
                }
            },
            modifier = modifier
        )
    } else {
        TopAppBar(
            title = { Text(title) },
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FullScheduleScreenPreview() {
    BusScheduleTheme {
        FullScheduleScreen(
            busSchedules = List(3) { index ->
                BusSchedule(
                    index,
                    "Main Street",
                    111111
                )
            },
            onScheduleClick = {},
            onAddClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RouteScheduleScreenPreview() {
    BusScheduleTheme {
        RouteScheduleScreen(
            stopName = "Main Street",
            busSchedules = List(3) { index ->
                BusSchedule(
                    index,
                    "Main Street",
                    111111
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddScheduleDialogPreview() {
    BusScheduleTheme {
        AddScheduleDialog(
            onDismissRequest = {},
            onConfirm = { _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DeleteConfirmationDialogPreview() {
    BusScheduleTheme {
        DeleteConfirmationDialog(
            onDismissRequest = {},
            onConfirm = {}
        )
    }
}
