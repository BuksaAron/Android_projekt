package com.example.androidproject.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.network.HabitRepository
import com.example.androidproject.network.ScheduleRepository
import com.example.androidproject.network.models.HabitResponse
import com.example.androidproject.utils.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateScheduleUiState(
    val habits: List<HabitResponse> = emptyList(),
    val selectedHabitId: Long? = null,
    val date: String = DateTimeUtils.todayDate(),
    val startTime: String = DateTimeUtils.currentTime(),
    val durationMinutes: String = "30",
    val repeatPattern: String = REPEAT_DAILY,
    val notes: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
) {
    companion object {
        const val REPEAT_NONE = "none"
        const val REPEAT_DAILY = "daily"
        const val REPEAT_WEEKDAYS = "weekdays"
        const val REPEAT_WEEKENDS = "weekends"
    }
}

class CreateScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val habitRepository = HabitRepository()
    private val scheduleRepository = ScheduleRepository()

    private val _uiState = MutableStateFlow(CreateScheduleUiState())
    val uiState: StateFlow<CreateScheduleUiState> = _uiState.asStateFlow()

    fun loadHabits() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            habitRepository.getHabits()
                .onSuccess { habits ->
                    _uiState.update { state ->
                        state.copy(
                            habits = habits,
                            selectedHabitId = state.selectedHabitId ?: habits.firstOrNull()?.id,
                            isLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.message ?: "Failed to load habits")
                    }
                }
        }
    }

    fun onHabitSelected(id: Long) { _uiState.update { it.copy(selectedHabitId = id, error = null) } }
    fun onDateChange(value: String) { _uiState.update { it.copy(date = value, error = null) } }
    fun onStartTimeChange(value: String) { _uiState.update { it.copy(startTime = value, error = null) } }

    fun onDurationChange(value: String) {
        if (value.all { it.isDigit() }) {
            _uiState.update { it.copy(durationMinutes = value, error = null) }
        }
    }

    fun onRepeatPatternChange(value: String) { _uiState.update { it.copy(repeatPattern = value, error = null) } }
    fun onNotesChange(value: String) { _uiState.update { it.copy(notes = value, error = null) } }

    fun createSchedule() {
        val state = _uiState.value
        if (state.isSaving) return

        val duration = state.durationMinutes.toIntOrNull()
        val validationError = when {
            state.selectedHabitId == null -> "Choose a habit"
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
        val notes = state.notes.trim().ifBlank { null }

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val result = if (state.repeatPattern == CreateScheduleUiState.REPEAT_NONE) {
                scheduleRepository.createCustomSchedule(
                    habitId = state.selectedHabitId!!,
                    date = state.date,
                    startTime = startIso,
                    endTime = endIso,
                    durationMinutes = duration,
                    notes = notes
                ).map { listOf(it) }
            } else {
                scheduleRepository.createRecurringSchedule(
                    habitId = state.selectedHabitId!!,
                    startTime = startIso,
                    endTime = endIso,
                    durationMinutes = duration,
                    repeatPattern = state.repeatPattern,
                    repeatDays = 30,
                    notes = notes
                )
            }

            result
                .onSuccess { _uiState.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = throwable.message ?: "Failed to create schedule"
                        )
                    }
                }
        }
    }
}