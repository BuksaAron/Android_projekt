package com.example.androidproject.ui.schedule

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidproject.FormHeader
import com.example.androidproject.categoryEmoji
import com.example.androidproject.network.models.ProgressResponseDto
import com.example.androidproject.network.models.ScheduleResponse
import com.example.androidproject.utils.DateTimeUtils

@Composable
fun ScheduleDetailsScreen(
    scheduleId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: ScheduleDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(scheduleId) { viewModel.load(scheduleId) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            Toast.makeText(context, "Schedule deleted", Toast.LENGTH_SHORT).show()
            onDeleted()
        }
    }

    val schedule = uiState.schedule

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FormHeader("Schedule details", schedule?.habit?.name ?: "", onBack)

        if (schedule != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit schedule") },
                            onClick = {
                                menuExpanded = false
                                onEdit(scheduleId)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete schedule", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                viewModel.requestDelete()
                            }
                        )
                    }
                }
            }
        }

        uiState.error?.let { message ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    message,
                    modifier = Modifier.padding(14.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (schedule == null) {
            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else {
            HabitCard(schedule)
            TimeCard(schedule)
            ProgressCard(schedule)
            ParticipantsCard(schedule)

            NotesCard(
                notes = schedule.notes,
                isEditing = uiState.isEditingNotes,
                draft = uiState.notesDraft,
                isSaving = uiState.isSavingNotes,
                onEdit = viewModel::startEditNotes,
                onDraftChange = viewModel::onNotesDraftChange,
                onSave = viewModel::saveNotes,
                onCancel = viewModel::cancelEditNotes
            )

            RecentActivities(schedule.progress)
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isDeleting) viewModel.dismissDelete() },
            title = { Text("Delete schedule") },
            text = { Text("Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmDelete,
                    enabled = !uiState.isDeleting
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete, enabled = !uiState.isDeleting) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun HabitCard(schedule: ScheduleResponse) {
    val habit = schedule.habit ?: return
    SectionCard("Habit") {
        Text(
            "${categoryEmoji(habit.category.name)} ${habit.name}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Goal: ${habit.goal}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        habit.description?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TimeCard(schedule: ScheduleResponse) {
    val dateText = (schedule.date ?: schedule.startTime).orEmpty().take(10)
    SectionCard("When") {
        Text("📅 ${DateTimeUtils.formatDisplayDate(dateText)}", style = MaterialTheme.typography.bodyMedium)
        Text(
            "🕒 ${DateTimeUtils.formatScheduleTime(schedule.startTime, schedule.endTime, schedule.durationMinutes)}",
            style = MaterialTheme.typography.bodyMedium
        )
        schedule.durationMinutes?.let {
            Text("⏱ $it min", style = MaterialTheme.typography.bodyMedium)
        }
        schedule.status?.let {
            Text("Status: $it", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun completionFraction(schedule: ScheduleResponse): Float {
    if (schedule.progress.isEmpty()) {
        return if (schedule.status.equals("Completed", ignoreCase = true)) 1f else 0f
    }
    return schedule.progress.count { it.isCompleted }.toFloat() / schedule.progress.size
}

@Composable
private fun ProgressCard(schedule: ScheduleResponse) {
    val fraction = completionFraction(schedule)
    SectionCard("Progress") {
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        val done = schedule.progress.count { it.isCompleted }
        Text(
            if (schedule.progress.isEmpty()) "${(fraction * 100).toInt()}% complete"
            else "$done of ${schedule.progress.size} completed (${(fraction * 100).toInt()}%)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ParticipantsCard(schedule: ScheduleResponse) {
    if (schedule.participants.isEmpty()) return
    SectionCard("Participants") {
        schedule.participants.forEach { p ->
            Text("👤 ${p.name ?: p.email ?: "User ${p.id}"}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NotesCard(
    notes: String?,
    isEditing: Boolean,
    draft: String,
    isSaving: Boolean,
    onEdit: () -> Unit,
    onDraftChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    SectionCard("Notes") {
        if (isEditing) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                minLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save")
                    }
                }
                OutlinedButton(onClick = onCancel, enabled = !isSaving, shape = RoundedCornerShape(12.dp)) {
                    Text("Cancel")
                }
            }
        } else {
            Text(
                notes?.takeIf { it.isNotBlank() } ?: "No notes yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (notes.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onEdit) { Text("Edit notes") }
        }
    }
}

@Composable
private fun RecentActivities(progress: List<ProgressResponseDto>) {
    SectionCard("Recent activities") {
        if (progress.isEmpty()) {
            Text(
                "No activity logged yet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@SectionCard
        }
        progress
            .sortedByDescending { it.date.orEmpty() }
            .forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            DateTimeUtils.formatDisplayDate(item.date.orEmpty().take(10)),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(if (item.isCompleted) "✅ Done" else "⬜ Not done", style = MaterialTheme.typography.bodySmall)
                    }
                    item.loggedTime?.let {
                        Text("Logged: ${"%.0f".format(it)} min", style = MaterialTheme.typography.bodySmall)
                    }
                    item.notes?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
    }
}