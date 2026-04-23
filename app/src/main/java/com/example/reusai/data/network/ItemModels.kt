package com.example.reusai.data.network

data class ItemRequest(
    val title: String,
    val category: String,
    val description: String,
    val availableToChange: Boolean,
    val status: StatusEnum,
    val imageUrl: String
)

data class ItemResponse(
    val id: String,
    val status: String
)

enum class StatusEnum {
    NEW, USED
}