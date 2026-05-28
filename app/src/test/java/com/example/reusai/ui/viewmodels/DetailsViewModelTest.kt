package com.example.reusai.ui.viewmodels

import app.cash.turbine.test
import com.example.reusai.data.network.UserResponse
import com.example.reusai.data.network.UserSession
import com.example.reusai.data.repository.AuthRepository
import com.example.reusai.data.repository.ItemRepository
import com.example.reusai.data.repository.ItemUIModel
import com.example.reusai.data.repository.ProposalRepository
import com.example.reusai.data.network.TokenManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.coVerify
import io.mockk.verify
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.mockkObject
import io.mockk.unmockkObject
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
class DetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var itemRepository: ItemRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var proposalRepository: ProposalRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: DetailsViewModel

    private val mockItem = ItemUIModel(
        id = "item-1", title = "Test Item", category = "Cat", description = "Desc",
        imageUrl = "url", distance = "1km", rating = 5.0, ownerName = "Old Name",
        ownerPhotoUrl = "", ownerRating = 5.0, ownerPlatformTime = "", ownerTradesCount = 0,
        idUser = "user-owner-id"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        itemRepository = mockk()
        tokenManager = mockk()
        proposalRepository = mockk(relaxed = true)
        authRepository = mockk()

        every { tokenManager.getUserSession() } returns UserSession("my-user-id", "me@test.com")
        
        viewModel = DetailsViewModel(
            itemRepository,
            tokenManager,
            proposalRepository,
            authRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadItem success should update item and fetch owner details`() = runTest {
        val ownerDetails = UserResponse("user-owner-id", "Real Owner Name", "owner@test.com", "photo-url")
        coEvery { itemRepository.getItemById("item-1") } returns mockItem
        coEvery { authRepository.getUser("user-owner-id") } returns Result.success(ownerDetails)

        viewModel.loadItem("item-1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Real Owner Name", state.item?.ownerName)
        assertEquals("photo-url", state.item?.ownerPhotoUrl)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadItem should show error when item not found`() = runTest {
        coEvery { itemRepository.getItemById("any") } returns null

        viewModel.loadItem("any")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Item não encontrado", viewModel.uiState.value.error)
    }

    @Test
    fun `loadUserItems should update state with current user items`() = runTest {
        val myItems = listOf(mockItem.copy(id = "my-item-1"))
        coEvery { itemRepository.getItemsByUser("my-user-id") } returns myItems

        viewModel.loadUserItems()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(myItems, viewModel.uiState.value.userItems)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadUserItems should fail if user not authenticated`() = runTest {
        every { tokenManager.getUserSession() } returns null

        // Recria a ViewModel para garantir que o estado inicial considere a sessão nula
        viewModel = DetailsViewModel(itemRepository, tokenManager, proposalRepository, authRepository)

        viewModel.loadUserItems()

        // ADICIONE ESTA LINHA ABAIXO:
        testDispatcher.scheduler.advanceUntilIdle() // Aguarda a execução da corrotina disparada pelo launch

        assertEquals("Usuário não autenticado", viewModel.uiState.value.error)
    }

    @Test
    fun `confirmTrade success should set isTradeSuccess to true`() = runTest {
        // Setup state: load target item and select my item
        coEvery { itemRepository.getItemById("target-1") } returns mockItem.copy(id = "target-1")
        coEvery { authRepository.getUser(any()) } returns Result.success(mockk(relaxed = true))
        viewModel.loadItem("target-1")
        
        val myItem = mockItem.copy(id = "my-offer-1")
        viewModel.selectUserItem(myItem)
        
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { proposalRepository.createProposal(any()) } returns mockk()

        viewModel.confirmTrade()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isTradeSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `confirmTrade should show error if no item is selected for trade`() = runTest {
        coEvery { itemRepository.getItemById("target-1") } returns mockItem
        coEvery { authRepository.getUser(any()) } returns Result.success(mockk(relaxed = true))
        viewModel.loadItem("target-1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.confirmTrade()

        assertEquals("Selecione um item para oferecer", viewModel.uiState.value.error)
    }

    @Test
    fun `toggleFavorite should switch boolean state`() = runTest {
        assertFalse(viewModel.uiState.value.isFavorite)
        
        viewModel.toggleFavorite()
        assertTrue(viewModel.uiState.value.isFavorite)
        
        viewModel.toggleFavorite()
        assertFalse(viewModel.uiState.value.isFavorite)
    }
}
