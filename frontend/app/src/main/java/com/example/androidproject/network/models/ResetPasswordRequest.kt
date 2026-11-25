package com.example.androidproject.network.models

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val email: String
)