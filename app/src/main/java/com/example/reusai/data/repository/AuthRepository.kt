package com.example.reusai.data.repository

import com.example.reusai.data.network.LoginRequest
import com.example.reusai.data.network.UserRequest
import com.example.reusai.data.network.UserResponse
import kotlinx.coroutines.delay

class AuthRepository {
    suspend fun login(loginRequest: LoginRequest): Result<UserResponse> {
        delay(1500)

        return if (loginRequest.email == "max@gmail.com" && loginRequest.password == "123") {
            Result.success(
                UserResponse(
                    id = "1",
                    username = "Max",
                    email = "max@gmail.com",
                    token = "mock-token-123"
                )
            )
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    suspend fun logout(): Result<Unit> {
        delay(500) // Simulate network/io delay
        // Here you would clear tokens from DataStore/SharedPreferences
        return Result.success(Unit)
    }
}
