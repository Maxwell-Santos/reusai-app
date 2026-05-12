package com.example.reusai.data.network

data class ItemRequest(
    val title: String,
    val category: String,
    val description: String,
    val availableToChange: Boolean,
    val status: StatusEnum,
    val imageUrl: String,
    val idUser: String
)

data class ItemResponse(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val imageUrl: String,
    val availableToChange: Boolean,
    val status: String,
    val idUser: String
)

enum class StatusEnum {
    NEW, USED
}