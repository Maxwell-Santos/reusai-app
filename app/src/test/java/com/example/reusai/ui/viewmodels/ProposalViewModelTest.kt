package com.example.reusai.ui.viewmodels

import app.cash.turbine.test
import com.example.reusai.data.network.ItemResponse
import com.example.reusai.data.network.ProposalResponse
import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.network.UserResponse
import com.example.reusai.data.network.UserSession
import com.example.reusai.data.repository.ProposalRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProposalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ProposalRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: ProposalViewModel

    private val mockUser = UserResponse("u1", "User 1", "user1@test.com")
    private val mockItem = ItemResponse("i1", "Item 1", "Cat", "Desc", "url", true, "NEW", "u1")
    
    private val mockProposal = ProposalResponse(
        id = "p1",
        userFrom = mockUser,
        itemFrom = mockItem,
        userTo = mockUser,
        itemTo = mockItem,
        statusProposal = "CREATED"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        tokenManager = mockk()
        
        // Default: Authenticated user
        every { tokenManager.getUserSession() } returns UserSession("u1", "user1@test.com")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProposals should update state with received and sent proposals`() = runTest {
        coEvery { repository.getProposalsReceived("u1") } returns listOf(mockProposal)
        coEvery { repository.getProposalsSent("u1") } returns emptyList()

        viewModel = ProposalViewModel(repository, tokenManager)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.receivedProposals.size)
        assertTrue(state.sentProposals.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadProposals should show error when user is not authenticated`() = runTest {
        every { tokenManager.getUserSession() } returns null

        viewModel = ProposalViewModel(repository, tokenManager)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Usuário não autenticado", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `selectTab should update selectedTab index`() = runTest {
        viewModel = ProposalViewModel(repository, tokenManager)
        
        viewModel.selectTab(1)
        
        assertEquals(1, viewModel.uiState.value.selectedTab)
    }

    @Test
    fun `acceptProposal should call repository and refresh list`() = runTest {
        coEvery { repository.getProposalsReceived("u1") } returns listOf(mockProposal)
        viewModel = ProposalViewModel(repository, tokenManager)
        testDispatcher.scheduler.advanceUntilIdle()

        // O teste deve começar ANTES da ação
        viewModel.uiState.test {
            // Pula o estado inicial emitido pelo loadProposals() do init
            skipItems(1)

            viewModel.acceptProposal("p1")

            // Agora o awaitItem() pegará o isLoading = true disparado pelo acceptProposal
            assertTrue(awaitItem().isLoading)

            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { repository.acceptProposal("p1") }
            coVerify { repository.getProposalsReceived("u1") }

            // Opcional: verificar que isLoading voltou para false
            assertFalse(awaitItem().isLoading)
        }
    }

    @Test
    fun `rejectProposal should call repository and refresh list`() = runTest {
        viewModel = ProposalViewModel(repository, tokenManager)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.rejectProposal("p1")
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        coVerify { repository.rejectProposal("p1") }
        coVerify(exactly = 2) { repository.getProposalsSent("u1") }
    }

    @Test
    fun `repository failure should show error message`() = runTest {
        coEvery { repository.getProposalsReceived(any()) } throws Exception("API Failure")
        
        viewModel = ProposalViewModel(repository, tokenManager)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("API Failure", viewModel.uiState.value.error)
    }
}
