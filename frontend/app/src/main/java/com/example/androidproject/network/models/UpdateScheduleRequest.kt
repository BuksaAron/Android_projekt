package com.example.androidproject.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateScheduleRequest(
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    val status: String? = null,
    val date: String? = null,
    @SerialName("is_custom") val isCustom: Boolean? = null,
    val participantIds: List<Long>? = null,
    val notes: String? = null
)