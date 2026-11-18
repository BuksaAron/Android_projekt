package com.example.androidproject.network

import com.example.androidproject.network.models.CreateCustomScheduleRequest
import com.example.androidproject.network.models.CreateRecurringScheduleRequest
import com.example.androidproject.network.models.ScheduleResponse
import com.example.androidproject.network.models.UpdateScheduleRequest

class ScheduleRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    suspend fun getScheduleByDay(day: String): Result<List<ScheduleResponse>> = runCatching {
        api.getScheduleByDay(day).bodyOrThrow("Failed to load schedules")
    }

    suspend fun getScheduleById(id: Long): Result<ScheduleResponse> = runCatching {
        api.getScheduleById(id).bodyOrThrow("Failed to load schedule")
    }

    suspend fun updateSchedule(id: Long, request: UpdateScheduleRequest): Result<ScheduleResponse> =
        runCatching {
            api.updateSchedule(id, request).bodyOrThrow("Failed to update schedule")
        }

    suspend fun deleteSchedule(id: Long): Result<Unit> = runCatching {
        api.deleteSchedule(id).unitOrThrow("Failed to delete schedule")
    }

    suspend fun createRecurringSchedule(
        habitId: Long,
        startTime: String,
        endTime: String?,
        durationMinutes: Int,
        repeatPattern: String,
        repeatDays: Int,
        notes: String?
    ): Result<List<ScheduleResponse>> = runCatching {
        api.createRecurringSchedule(
            CreateRecurringScheduleRequest(
                habitId = habitId,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                repeatPattern = repeatPattern,
                repeatDays = repeatDays,
                notes = notes
            )
        ).bodyOrThrow("Failed to create schedule")
    }

    suspend fun createCustomSchedule(
        habitId: Long,
        date: String,
        startTime: String,
        endTime: String?,
        durationMinutes: Int,
        notes: String?
    ): Result<ScheduleResponse> = runCatching {
        api.createCustomSchedule(
            CreateCustomScheduleRequest(
                habitId = habitId,
                date = date,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                notes = notes
            )
        ).bodyOrThrow("Failed to create schedule")
    }
}