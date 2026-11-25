package com.example.androidproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidproject.network.AuthRepository
import com.example.androidproject.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SplashDestination { Home, Login }

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val sessionManager = SessionManager.getInstance(application)

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            val target = checkAutoLogin()

            // the animation always gets its minimum time, even if the check is instant
            val remaining = MIN_SPLASH_MS - (System.currentTimeMillis() - startedAt)
            if (remaining > 0) delay(remaining)

            _destination.value = target
        }
    }

    private suspend fun checkAutoLogin(): SplashDestination {
        val refreshToken = sessionManager.refreshToken
        if (refreshToken.isNullOrEmpty()) return SplashDestination.Login

        return repository.refresh(refreshToken).fold(
            onSuccess = { tokens ->
                val newAccess = tokens.accessToken
                if (newAccess.isNullOrEmpty()) {
                    sessionManager.clearSession()
                    SplashDestination.Login
                } else {
                    sessionManager.updateTokens(newAccess, tokens.refreshToken ?: refreshToken)
                    SplashDestination.Home
                }
            },
            onFailure = {
                sessionManager.clearSession()
                SplashDestination.Login
            }
        )
    }

    private companion object {
        const val MIN_SPLASH_MS = 1500L
    }
}