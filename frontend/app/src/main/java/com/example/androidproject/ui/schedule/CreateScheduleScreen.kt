package com.example.androidproject.ui.schedule

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidproject.utils.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CreateScheduleScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onNavigateToAddHabit: () -> Unit,
    viewModel: CreateScheduleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadHabits() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "Schedule created", Toast.LENGTH_SHORT).show()
            onSaved()
        }
    }

    Column {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.habits, key = { it.id }) { habit ->
                FilterChip(
                    selected = uiState.selectedHabitId == habit.id,
                    onClick = { viewModel.onHabitSelected(habit.id) },
                    label = { Text(habit.name) }
                )
            }
        }

        OutlinedButton(onClick = {
            showDatePicker(context, uiState.date, viewModel::onDateChange)
        }) { Text(DateTimeUtils.formatDisplayDate(uiState.date)) }

        OutlinedButton(onClick = {
            showTimePicker(context, uiState.startTime, viewModel::onStartTimeChange)
        }) { Text(uiState.startTime) }

        OutlinedTextField(
            value = uiState.durationMinutes,
            onValueChange = viewModel::onDurationChange,
            label = { Text("Duration (minutes)") }
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("none", "daily", "weekdays", "weekends")) { pattern ->
                FilterChip(
                    selected = uiState.repeatPattern == pattern,
                    onClick = { viewModel.onRepeatPatternChange(pattern) },
                    label = { Text(pattern) }
                )
            }
        }

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = viewModel::onNotesChange,
            label = { Text("Notes (optional)") }
        )

        Button(onClick = viewModel::createSchedule) { Text("Create schedule") }
    }
}

private fun showDatePicker(context: Context, currentDate: String, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(currentDate)
    }.getOrNull()?.let { calendar.time = it }

    android.app.DatePickerDialog(
        context,
        { _, year, month, day ->
            val selected = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
            }
            onDateSelected(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selected.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun showTimePicker(context: Context, currentTime: String, onTimeSelected: (String) -> Unit) {
    val parts = currentTime.split(":").mapNotNull { it.toIntOrNull() }
    val hour = parts.getOrNull(0) ?: 9
    val minute = parts.getOrNull(1) ?: 0

    android.app.TimePickerDialog(
        context,
        { _, h, m -> onTimeSelected(String.format(Locale.US, "%02d:%02d", h, m)) },
        hour,
        minute,
        true
    ).show()
}