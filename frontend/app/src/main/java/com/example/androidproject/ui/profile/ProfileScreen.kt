package com.example.androidproject

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidproject.ui.profile.ProfileViewModel
import com.example.androidproject.utils.SessionManager

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()

){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Profile Screen",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Name: ${uiState.username ?: "N/A"}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Email: ${uiState.email ?: "N/A"}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "ID: ${uiState.userId}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    Button(onClick = {
        SessionManager.getInstance(context).clearSession()
        onLogout()
    }) {
        Text("Log out")
    }
}