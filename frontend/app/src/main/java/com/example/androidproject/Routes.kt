package com.example.androidproject
import kotlinx.serialization.Serializable

@Serializable object Login
@Serializable object Home
@Serializable object MainGraph

@Serializable object AddHabit
@Serializable object CreateSchedule
@Serializable object Splash
@Serializable object ResetPassword
@Serializable
data class Profile(
    val userId: String = "guest",
    val userName: String = "Guest"
)