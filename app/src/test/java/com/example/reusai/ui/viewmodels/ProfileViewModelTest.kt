package com.example.reusai.ui.viewmodels

import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.network.UserResponse
import com.example.reusai.data.network.UserSession
import com.example.reusai.data.repository.AuthRepository
import com.example.reusai.data.repository.ItemRepository
import com.example.reusai.data.repository.ItemUIModel
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
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var itemRepository: ItemRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: ProfileViewModel

    private val mockUserSession = UserSession("user-123", "test@example.com")
    private val mockUserResponse = UserResponse("user-123", "Test User", "test@example.com", "photo-url")
    private val mockItem = ItemUIModel(
        id = "1", title = "Item", category = "Cat", description = "",
        imageUrl = "", distance = "", rating = 0.0, ownerName = "",
        ownerPhotoUrl = "", ownerRating = 0.0, ownerPlatformTime = "", ownerTradesCount = 0
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        itemRepository = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        every { tokenManager.getUserSession() } returns mockUserSession
        coEvery { authRepository.getUser(any()) } returns Result.success(mockUserResponse)
        coEvery { itemRepository.getItemsByUser(any()) } returns listOf(mockItem)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load user profile and items`() = runTest {
        viewModel = ProfileViewModel(itemRepository, tokenManager, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Test User", state.userName)
        assertEquals("photo-url", state.profilePhotoUrl)
        assertEquals(1, state.items.size)
        assertEquals(1, state.activeItems)
    }

    @Test
    fun `logout success should clear tokens and update state`() = runTest {
        coEvery { authRepository.logout() } returns Result.success(Unit)
        viewModel = ProfileViewModel(itemRepository, tokenManager, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLogoutSuccess)
        coVerify { tokenManager.clearTokens() }
    }

    @Test
    fun `logout failure should show error message`() = runTest {
        coEvery { authRepository.logout() } returns Result.failure(Exception("Logout failed"))
        viewModel = ProfileViewModel(itemRepository, tokenManager, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Erro ao sair", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLogoutSuccess)
    }

    @Test
    fun `loadUserItems should handle repository error`() = runTest {
        coEvery { itemRepository.getItemsByUser(any()) } throws Exception("Fetch error")
        viewModel = ProfileViewModel(itemRepository, tokenManager, authRepository)
        
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage?.contains("Erro ao carregar itens") == true)
    }

    @Test
    fun `clearError should reset error message in state`() = runTest {
        viewModel = ProfileViewModel(itemRepository, tokenManager, authRepository)
        
        coEvery { authRepository.logout() } returns Result.failure(Exception("Fail"))
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.errorMessage != null)
        
        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
