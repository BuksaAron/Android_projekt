package com.example.androidproject

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidproject.network.models.HabitResponse
import com.example.androidproject.network.models.ScheduleResponse
import com.example.androidproject.utils.DateTimeUtils
import com.example.androidproject.utils.HomeViewModel

@Composable
fun HomeScreen(
    onViewProfileClick: () -> Unit,
    onAddHabit: () -> Unit,
    onCreateSchedule: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadToday() }

    LazyColumn {
        item {
            Button(onClick = onAddHabit) { Text("Add habit") }
            Button(onClick = onCreateSchedule) { Text("New schedule") }
        }

        uiState.error?.let { message ->
            item { Text("Couldn't load your data: $message") }
        }

        item { Text("Today's schedule") }
        if (uiState.schedules.isEmpty()) {
            item { Text("Nothing scheduled today") }
        } else {
            items(uiState.schedules, key = { it.id }) { schedule ->
                ScheduleCard(schedule)
            }
        }

        item { Text("Habits") }
        if (uiState.habits.isEmpty()) {
            item { Text("No habits yet") }
        } else {
            items(uiState.habits, key = { it.id }) { habit ->
                HabitCard(habit)
            }
        }
    }
}

@Composable
private fun ScheduleCard(schedule: ScheduleResponse) {
    val categoryName = schedule.habit?.category?.name ?: "Other"
    val startTime = DateTimeUtils.formatTime(schedule.startTime).ifBlank { "--:--" }

    Card {
        Row {
            Column(Modifier.width(64.dp)) {
                Text(startTime)
                Text("${schedule.durationMinutes ?: 0} min")
            }
            Column(Modifier.weight(1f)) {
                Text(schedule.habit?.name ?: "Unknown habit")
                Text(categoryName)
                schedule.habit?.description?.takeIf { it.isNotBlank() }?.let { Text(it) }
                schedule.notes?.takeIf { it.isNotBlank() }?.let { Text(it) }
            }
        }
    }
}

@Composable
private fun HabitCard(habit: HabitResponse) {
    Card {
        Column {
            Text(habit.name)
            Text(habit.category.name)
            Text("Goal: ${habit.goal}")
            habit.description?.takeIf { it.isNotBlank() }?.let { Text(it) }
        }
    }
}