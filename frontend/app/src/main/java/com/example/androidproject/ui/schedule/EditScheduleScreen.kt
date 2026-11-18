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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidproject.FormHeader
import com.example.androidproject.utils.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EditScheduleScreen(
    scheduleId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditScheduleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(scheduleId) { viewModel.load(scheduleId) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "Schedule updated", Toast.LENGTH_SHORT).show()
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FormHeader("Edit schedule", uiState.habitName, onBack)

        uiState.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        if (uiState.isLoading && !uiState.loaded) {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            return@Column
        }
        if (!uiState.loaded) return@Column

        Text("When", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        OutlinedButton(
            onClick = { pickDate(context, uiState.date, viewModel::onDateChange) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) { Text("📅 ${DateTimeUtils.formatDisplayDate(uiState.date)}") }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { pickTime(context, uiState.startTime, viewModel::onStartTimeChange) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Start  ${uiState.startTime}") }
            OutlinedButton(
                onClick = { pickTime(context, uiState.endTime, viewModel::onEndTimeChange) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("End  ${uiState.endTime}") }
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

        Text("Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(EditScheduleViewModel.STATUSES) { status ->
                FilterChip(
                    selected = uiState.status == status,
                    onClick = { viewModel.onStatusChange(status) },
                    label = { Text(status) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Text("Participants", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        if (uiState.participants.isEmpty()) {
            Text(
                "No participants.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.participants, key = { it.id }) { p ->
                    AssistChip(
                        onClick = { viewModel.removeParticipant(p.id) },
                        label = { Text("${p.name ?: p.email ?: "User ${p.id}"}  ✕") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = viewModel::onNotesChange,
            label = { Text("Notes") },
            minLines = 3,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.save(scheduleId) },
            enabled = !uiState.isSaving,
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
                Text("Save changes")
            }
        }
    }
}

private fun pickDate(context: Context, currentDate: String, onDateSelected: (String) -> Unit) {
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

private fun pickTime(context: Context, currentTime: String, onTimeSelected: (String) -> Unit) {
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