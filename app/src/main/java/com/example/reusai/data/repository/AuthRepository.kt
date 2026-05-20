package com.example.reusai.data.repository

import com.example.reusai.data.network.AuthResponse
import com.example.reusai.data.network.LoginRequest
import com.example.reusai.data.network.RetrofitClient
import com.example.reusai.data.network.ReusaiApiService
import com.example.reusai.data.network.UserRequest
import com.example.reusai.data.network.UserResponse

class AuthRepository(
    private val apiService: ReusaiApiService = RetrofitClient.instance
) {
    suspend fun login(loginRequest: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(loginRequest)
            RetrofitClient.getTokenManager()?.saveTokens(response.accessToken, response.refreshToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(userRequest: UserRequest): Result<UserResponse> {
        return try {
            val response = apiService.createUser(userRequest)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            apiService.logout()
            RetrofitClient.getTokenManager()?.clearTokens()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshToken(username: String): Result<AuthResponse> {
        return try {
            val response = apiService.refreshToken(username)
            RetrofitClient.getTokenManager()?.saveTokens(response.accessToken, response.refreshToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
