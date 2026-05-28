package com.example.reusai.ui.viewmodels

import app.cash.turbine.test
import com.example.reusai.data.repository.ItemRepository
import com.example.reusai.data.repository.ItemUIModel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ItemRepository
    private lateinit var viewModel: HomeViewModel

    private val mockItems = listOf(
        ItemUIModel(
            id = "1", title = "Cadeira", category = "Casa", description = "",
            imageUrl = "", distance = "", rating = 0.0, ownerName = "",
            ownerPhotoUrl = "", ownerRating = 0.0, ownerPlatformTime = "", ownerTradesCount = 0
        ),
        ItemUIModel(
            id = "2", title = "Livro", category = "Livros", description = "",
            imageUrl = "", distance = "", rating = 0.0, ownerName = "",
            ownerPhotoUrl = "", ownerRating = 0.0, ownerPlatformTime = "", ownerTradesCount = 0
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load items from repository`() = runTest {
        coEvery { repository.getExploreItems() } returns mockItems

        viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            awaitItem() // Consome o 1. Estado Inicial

            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(awaitItem().isLoading) // Consome o 2. Estado de Carregamento

            val finalState = awaitItem() // Consome o 3. Estado de Sucesso (com os itens)
            assertEquals(mockItems, finalState.items)
            assertFalse(finalState.isLoading)
        }
    }

    @Test
    fun `loadItems failure should update error message`() = runTest {
        val errorMsg = "Network Error"
        coEvery { repository.getExploreItems() } throws Exception(errorMsg)

        viewModel = HomeViewModel(repository)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertEquals(errorMsg, state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onCategorySelected should filter items correctly`() = runTest {
        coEvery { repository.getExploreItems() } returns mockItems
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCategorySelected("Casa")

        val state = viewModel.uiState.value
        assertEquals("Casa", state.selectedCategory)
        assertEquals(1, state.filteredItems.size)
        assertEquals("1", state.filteredItems[0].id)
    }

    @Test
    fun `onSearchQueryChanged should filter items by title`() = runTest {
        coEvery { repository.getExploreItems() } returns mockItems
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChanged("livro")

        val state = viewModel.uiState.value
        assertEquals("livro", state.searchQuery)
        assertEquals(1, state.filteredItems.size)
        assertEquals("2", state.filteredItems[0].id)
    }

    @Test
    fun `filter should combine category and search`() = runTest {
        coEvery { repository.getExploreItems() } returns mockItems
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCategorySelected("Casa")
        viewModel.onSearchQueryChanged("Livro")

        val state = viewModel.uiState.value
        assertTrue(state.filteredItems.isEmpty())
    }

    @Test
    fun `selecting Todos category should show all items`() = runTest {
        coEvery { repository.getExploreItems() } returns mockItems
        viewModel = HomeViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCategorySelected("Casa")
        assertEquals(1, viewModel.uiState.value.filteredItems.size)

        viewModel.onCategorySelected("Todos")
        assertEquals(2, viewModel.uiState.value.filteredItems.size)
    }
}
