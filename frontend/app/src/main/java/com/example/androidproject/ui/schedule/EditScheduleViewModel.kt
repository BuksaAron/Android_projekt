package com.example.androidproject.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.network.ScheduleRepository
import com.example.androidproject.network.models.ParticipantDto
import com.example.androidproject.network.models.UpdateScheduleRequest
import com.example.androidproject.utils.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class EditScheduleUiState(
    val loaded: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val habitName: String = "",
    val date: String = DateTimeUtils.todayDate(),
    val startTime: String = "09:00",
    val endTime: String = "09:30",
    val durationMinutes: String = "30",
    val status: String = EditScheduleViewModel.STATUS_PLANNED,
    val participants: List<ParticipantDto> = emptyList(),
    val notes: String = ""
)

class EditScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ScheduleRepository()

    private val _uiState = MutableStateFlow(EditScheduleUiState())
    val uiState: StateFlow<EditScheduleUiState> = _uiState.asStateFlow()

    fun load(id: Long) {
        if (_uiState.value.loaded) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getScheduleById(id)
                .onSuccess { s ->
                    val start = DateTimeUtils.formatTime(s.startTime).ifBlank { "09:00" }
                    val end = DateTimeUtils.formatTime(s.endTime).ifBlank { start }
                    val duration = s.durationMinutes
                        ?: diffMinutes(start, end)?.takeIf { it > 0 }
                        ?: 30
                    val status = STATUSES.firstOrNull { it.equals(s.status, ignoreCase = true) }
                        ?: STATUS_PLANNED
                    _uiState.update {
                        it.copy(
                            loaded = true,
                            isLoading = false,
                            habitName = s.habit?.name.orEmpty(),
                            date = (s.date ?: s.startTime).orEmpty().take(10)
                                .ifBlank { DateTimeUtils.todayDate() },
                            startTime = start,
                            endTime = end,
                            durationMinutes = duration.toString(),
                            status = status,
                            participants = s.participants,
                            notes = s.notes.orEmpty()
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.message ?: "Failed to load schedule")
                    }
                }
        }
    }

    fun onDateChange(value: String) {
        _uiState.update { it.copy(date = value, error = null) }
    }
    fun onStartTimeChange(value: String) {
        _uiState.update { state ->
            val duration = state.durationMinutes.toIntOrNull()
            val start = toMinutes(value)
            val newEnd = if (duration != null && duration > 0 && start != null) {
                fromMinutes(start + duration)
            } else state.endTime
            state.copy(startTime = value, endTime = newEnd, error = null)
        }
    }

    fun onEndTimeChange(value: String) {
        _uiState.update { state ->
            val diff = diffMinutes(state.startTime, value)
            state.copy(
                endTime = value,
                durationMinutes = if (diff != null && diff > 0) diff.toString() else state.durationMinutes,
                error = null
            )
        }
    }

    fun onDurationChange(value: String) {
        if (!value.all { it.isDigit() }) return
        _uiState.update { state ->
            val duration = value.toIntOrNull()
            val start = toMinutes(state.startTime)
            val newEnd = if (duration != null && duration > 0 && start != null) {
                fromMinutes(start + duration)
            } else state.endTime
            state.copy(durationMinutes = value, endTime = newEnd, error = null)
        }
    }

    fun onStatusChange(value: String) {
        _uiState.update { it.copy(status = value, error = null) }
    }

    fun onNotesChange(value: String) {
        _uiState.update { it.copy(notes = value, error = null) }
    }

    fun removeParticipant(id: Long) {
        _uiState.update { state ->
            state.copy(participants = state.participants.filterNot { it.id == id }, error = null)
        }
    }

    fun save(id: Long) {
        val state = _uiState.value
        if (state.isSaving) return

        val duration = state.durationMinutes.toIntOrNull()
        val validationError = when {
            state.date.isBlank() -> "Choose a date"
            state.startTime.isBlank() -> "Choose a start time"
            duration == null || duration <= 0 -> "Duration must be at least 1 minute"
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        val startIso = DateTimeUtils.buildIsoDateTime(state.date, state.startTime)
        val endIso = DateTimeUtils.addMinutesToIso(startIso, duration!!)

        val request = UpdateScheduleRequest(
            startTime = startIso,
            endTime = endIso,
            durationMinutes = duration,
            status = state.status,
            date = state.date,
            participantIds = state.participants.map { it.id },
            notes = state.notes.trim()
        )

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            repository.updateSchedule(id, request)
                .onSuccess { _uiState.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = throwable.message ?: "Failed to update schedule"
                        )
                    }
                }
        }
    }

    private fun toMinutes(time: String): Int? {
        val parts = time.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.size < 2) return null
        return parts[0] * 60 + parts[1]
    }

    private fun fromMinutes(total: Int): String {
        val m = ((total % 1440) + 1440) % 1440
        return String.format(Locale.US, "%02d:%02d", m / 60, m % 60)
    }

    private fun diffMinutes(start: String, end: String): Int? {
        val s = toMinutes(start) ?: return null
        val e = toMinutes(end) ?: return null
        return e - s
    }

    companion object {
        const val STATUS_PLANNED = "Planned"
        const val STATUS_COMPLETED = "Completed"
        const val STATUS_SKIPPED = "Skipped"
        val STATUSES = listOf(STATUS_PLANNED, STATUS_COMPLETED, STATUS_SKIPPED)
    }
}