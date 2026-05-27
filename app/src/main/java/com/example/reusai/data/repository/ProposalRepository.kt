package com.example.reusai.data.repository

import com.example.reusai.data.network.ProposalRequest
import com.example.reusai.data.network.ProposalResponse
import com.example.reusai.data.network.ReusaiApiService

class ProposalRepository(private val apiService: ReusaiApiService) {

    suspend fun getProposalsReceived(userId: String): List<ProposalResponse> {
        return apiService.getProposalsReceived(userId)
    }

    suspend fun getProposalsSent(userId: String): List<ProposalResponse> {
        return apiService.getProposalsSent(userId)
    }

    suspend fun createProposal(proposal: ProposalRequest): ProposalResponse {
        return apiService.createProposal(proposal)
    }

    suspend fun acceptProposal(proposalId: String): ProposalResponse {
        return apiService.acceptProposal(proposalId)
    }

    suspend fun rejectProposal(proposalId: String): ProposalResponse {
        return apiService.rejectProposal(proposalId)
    }
}
