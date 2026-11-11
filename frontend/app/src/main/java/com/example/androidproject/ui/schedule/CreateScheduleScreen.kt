package com.example.androidproject.ui.schedule

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.androidproject.FormHeader
import com.example.androidproject.categoryEmoji


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

    val repeatOptions = listOf(
        "none" to "Once",
        "daily" to "Daily",
        "weekdays" to "Weekdays",
        "weekends" to "Weekends"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FormHeader("New schedule", "Choose a habit, time, and repeat pattern.", onBack)

        Text("Habit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        if (uiState.habits.isEmpty() && !uiState.isLoading) {
            Text(
                "You have no habits yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.habits, key = { it.id }) { habit ->
                FilterChip(
                    selected = uiState.selectedHabitId == habit.id,
                    onClick = { viewModel.onHabitSelected(habit.id) },
                    label = { Text("${categoryEmoji(habit.category.name)} ${habit.name}") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Text("When", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { showDatePicker(context, uiState.date, viewModel::onDateChange) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("📅 ${DateTimeUtils.formatDisplayDate(uiState.date)}") }
            OutlinedButton(
                onClick = { showTimePicker(context, uiState.startTime, viewModel::onStartTimeChange) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("🕒 ${uiState.startTime}") }
        }

        OutlinedTextField(
            value = uiState.durationMinutes,
            onValueChange = viewModel::onDurationChange,
            label = { Text("Duration (minutes)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Text("Repeat", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(repeatOptions) { (value, label) ->
                FilterChip(
                    selected = uiState.repeatPattern == value,
                    onClick = { viewModel.onRepeatPatternChange(value) },
                    label = { Text(label) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = viewModel::onNotesChange,
            label = { Text("Notes (optional)") },
            minLines = 2,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = viewModel::createSchedule,
            enabled = !uiState.isSaving && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create schedule")
            }
        }
        TextButton(
            onClick = onNavigateToAddHabit,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) { Text("Add another habit") }
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