package com.example.reusai.data.network

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
