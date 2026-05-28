package com.example.reusai.data.repository

import com.example.reusai.data.network.ProposalRequest
import com.example.reusai.data.network.ProposalResponse
import com.example.reusai.data.network.ReusaiApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProposalRepositoryTest {

    private lateinit var apiService: ReusaiApiService
    private lateinit var repository: ProposalRepository

    private val mockProposalResponse = mockk<ProposalResponse>()

    @Before
    fun setup() {
        apiService = mockk()
        repository = ProposalRepository(apiService)
    }

    @Test
    fun `getProposalsReceived should call API and return list`() = runTest {
        val userId = "user-1"
        val expectedList = listOf(mockProposalResponse)
        coEvery { apiService.getProposalsReceived(userId) } returns expectedList

        val result = repository.getProposalsReceived(userId)

        assertEquals(expectedList, result)
    }

    @Test
    fun `getProposalsSent should call API and return list`() = runTest {
        val userId = "user-1"
        val expectedList = listOf(mockProposalResponse)
        coEvery { apiService.getProposalsSent(userId) } returns expectedList

        val result = repository.getProposalsSent(userId)

        assertEquals(expectedList, result)
    }

    @Test
    fun `createProposal should call API with request and return response`() = runTest {
        val request = ProposalRequest("u1", "u2", "i1", "i2")
        coEvery { apiService.createProposal(request) } returns mockProposalResponse

        val result = repository.createProposal(request)

        assertEquals(mockProposalResponse, result)
    }

    @Test
    fun `acceptProposal should call API and return response`() = runTest {
        val proposalId = "p1"
        coEvery { apiService.acceptProposal(proposalId) } returns mockProposalResponse

        val result = repository.acceptProposal(proposalId)

        assertEquals(mockProposalResponse, result)
    }

    @Test
    fun `rejectProposal should call API and return response`() = runTest {
        val proposalId = "p1"
        coEvery { apiService.rejectProposal(proposalId) } returns mockProposalResponse

        val result = repository.rejectProposal(proposalId)

        assertEquals(mockProposalResponse, result)
    }

    @Test(expected = Exception::class)
    fun `repository should propagate API exceptions`() = runTest {
        coEvery { apiService.getProposalsReceived(any()) } throws Exception("Network Error")
        
        repository.getProposalsReceived("any")
    }
}
