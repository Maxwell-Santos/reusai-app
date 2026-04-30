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
                    name = "Max",
                    email = "max@gmail.com",
                    token = "mock-token-123"
                )
            )
        } else {
            Result.failure(Exception("Invalid email or password"))
        }
    }

    suspend fun register(userRequest: UserRequest): Result<UserResponse> {
        delay(1500)
        return Result.success(
            UserResponse(
                id = "2",
                name = userRequest.name,
                email = userRequest.email,
                message = "User created successfully"
            )
        )
    }
}
