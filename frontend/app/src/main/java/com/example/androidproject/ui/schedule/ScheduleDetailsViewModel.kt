package com.example.androidproject.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.network.ScheduleRepository
import com.example.androidproject.network.models.ScheduleResponse
import com.example.androidproject.network.models.UpdateScheduleRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleDetailsUiState(
    val schedule: ScheduleResponse? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isEditingNotes: Boolean = false,
    val notesDraft: String = "",
    val isSavingNotes: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false
)

class ScheduleDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ScheduleRepository()

    private val _uiState = MutableStateFlow(ScheduleDetailsUiState())
    val uiState: StateFlow<ScheduleDetailsUiState> = _uiState.asStateFlow()

    fun load(id: Long) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getScheduleById(id)
                .onSuccess { schedule ->
                    _uiState.update { it.copy(schedule = schedule, isLoading = false) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.message ?: "Failed to load schedule")
                    }
                }
        }
    }

    fun startEditNotes() {
        _uiState.update { it.copy(isEditingNotes = true, notesDraft = it.schedule?.notes.orEmpty()) }
    }

    fun onNotesDraftChange(value: String) {
        _uiState.update { it.copy(notesDraft = value) }
    }

    fun cancelEditNotes() {
        _uiState.update { it.copy(isEditingNotes = false, notesDraft = "") }
    }

    fun saveNotes() {
        val state = _uiState.value
        val current = state.schedule ?: return
        if (state.isSavingNotes) return

        _uiState.update { it.copy(isSavingNotes = true, error = null) }
        viewModelScope.launch {
            repository.updateSchedule(current.id, UpdateScheduleRequest(notes = state.notesDraft.trim()))
                .onSuccess { updated ->
                    val merged = updated.copy(
                        habit = updated.habit ?: current.habit,
                        progress = updated.progress.ifEmpty { current.progress },
                        participants = updated.participants.ifEmpty { current.participants }
                    )
                    _uiState.update {
                        it.copy(
                            schedule = merged,
                            isSavingNotes = false,
                            isEditingNotes = false,
                            notesDraft = ""
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSavingNotes = false,
                            error = throwable.message ?: "Failed to save notes"
                        )
                    }
                }
        }
    }

    fun requestDelete() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDelete() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        val state = _uiState.value
        val current = state.schedule ?: return
        if (state.isDeleting) return

        _uiState.update { it.copy(isDeleting = true, error = null) }
        viewModelScope.launch {
            repository.deleteSchedule(current.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(isDeleting = false, showDeleteDialog = false, isDeleted = true)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            showDeleteDialog = false,
                            error = throwable.message ?: "Failed to delete schedule"
                        )
                    }
                }
        }
    }
}