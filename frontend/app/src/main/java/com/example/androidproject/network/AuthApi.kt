package com.example.androidproject.network

import com.example.androidproject.network.models.LoginRequest
import com.example.androidproject.network.models.LoginResponse
import com.example.androidproject.network.models.ResetPasswordRequest
import com.example.androidproject.network.models.TokensDto

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AuthApi {
    @POST("auth/local/signin")
    suspend fun signIn(@Body body: LoginRequest): Response<LoginResponse>

    @Multipart
    @POST("auth/local/signup")
    suspend fun signUp(
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part profileImage: MultipartBody.Part?
    ): Response<LoginResponse>

    @POST("auth/reset-password-via-email")
    suspend fun resetPasswordViaEmail(@Body body: ResetPasswordRequest): Response<Unit>

    @POST("auth/local/refresh")
    suspend fun refresh(@Header("Authorization") bearer: String): Response<TokensDto>
}