package com.example.androidproject.ui.habit

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AddHabitScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onNavigateToCreateSchedule: () -> Unit,
    viewModel: AddHabitViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            Toast.makeText(context, "Habit created", Toast.LENGTH_SHORT).show()
            onSaved()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Name") }
        )
        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Description (optional)") }
        )
        OutlinedTextField(
            value = uiState.goal,
            onValueChange = viewModel::onGoalChange,
            label = { Text("Goal") }
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.categories, key = { it.id }) { category ->
                FilterChip(
                    selected = uiState.selectedCategoryId == category.id,
                    onClick = { viewModel.onCategorySelected(category.id) },
                    label = { Text(category.name) }
                )
            }
        }

        Button(
            onClick = viewModel::createHabit,
            enabled = !uiState.isSaving && !uiState.isLoadingCategories
        ) { Text("Create habit") }

        TextButton(onClick = onNavigateToCreateSchedule) {
            Text("Schedule an existing habit")
        }
    }
}