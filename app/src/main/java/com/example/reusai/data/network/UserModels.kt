package com.example.reusai.data.network

import java.util.Date

data class UserRequest(
    val username: String,
    val cep: String,
    val email: String,
    val password: String,
    val photoUrl: String? = null
)

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val message: String? = null,
    val token: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val username: String,
    val authenticated: Boolean,
    val created: Date,
    val expiration: Date,
    val accessToken: String,
    val refreshToken: String
)

data class UserSession(
    val id: String,
    val email: String
)
