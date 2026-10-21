package com.example.androidproject
import kotlinx.serialization.Serializable

@Serializable object Login
@Serializable object Home
@Serializable object MainGraph

@Serializable
data class Profile(
    val userId: String = "guest",
    val userName: String = "Guest"
)