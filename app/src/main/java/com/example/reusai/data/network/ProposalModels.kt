package com.example.reusai.data.network

data class ProposalRequest(
    val idUserFrom: String,
    val idUserTo: String,
    val idItemFrom: String,
    val idItemTo: String
)

data class ProposalResponse(
    val id: String,
    val userFrom: UserResponse,
    val itemFrom: ItemResponse,
    val userTo: UserResponse,
    val itemTo: ItemResponse,
    val statusProposal: String,
    val createdAt: String? = null
)

enum class ProposalStatus {
    CREATED, ACCEPTED, REJECTED
}
