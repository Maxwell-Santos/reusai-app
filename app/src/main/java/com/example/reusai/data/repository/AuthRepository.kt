package com.example.reusai.data.repository

import com.example.reusai.data.network.AuthResponse
import com.example.reusai.data.network.LoginRequest
import com.example.reusai.data.network.RetrofitClient
import com.example.reusai.data.network.ReusaiApiService
import com.example.reusai.data.network.UserRequest
import com.example.reusai.data.network.UserResponse
import kotlin.String

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
        try {
            apiService.logout()
        } catch (e: Exception) {
            // We ignore API failures on logout to ensure the user can always
            // leave the session locally.
        }
        RetrofitClient.getTokenManager()?.clearTokens()
        return Result.success(Unit)
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

    suspend fun getUser(userId: String): Result<UserResponse> {
        return try {
//            val response = apiService.getUser(userId)
            val user = if (userId == "2d7211ca-6687-4f1f-a224-1140ac87a827")
                UserResponse(
                     id = "2d7211ca-6687-4f1f-a224-1140ac87a827",
                username = "Leonardo",
                email = "leo@gmail.com",
                photoUrl = "https://rnsyrzyzguctpqmiquom.supabase.co/storage/v1/object/public/images/2d65e48b-e303-4a54-ad83-fb0649054a29_leo.png",
                message = null,
                token = null)
                else UserResponse(
                    id = "8d0c3c8c-0fe1-45d1-bb81-bca46279cb15",
                    username = "Maxwell",
                    email = "max@gmail.com",
                    photoUrl = "https://rnsyrzyzguctpqmiquom.supabase.co/storage/v1/object/public/images/0c8313fd-8627-47f1-aad6-405001b5bcd4_max.png",
                    message = null,
                    token = null
                )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
