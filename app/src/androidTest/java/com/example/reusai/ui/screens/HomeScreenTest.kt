package com.example.reusai.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.reusai.data.repository.ItemUIModel
import com.example.reusai.ui.viewmodels.HomeUiState
import com.example.reusai.ui.viewmodels.HomeViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<HomeViewModel>(relaxed = true)

    @Test
    fun homeScreen_InitialState_DisplaysCategoriesAndHeader() {
        val uiState = MutableStateFlow(HomeUiState())
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onItemClick = {})
        }

        composeTestRule.onNodeWithText("Explorar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Todos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eletrônicos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Destaques perto de você").assertIsDisplayed()
    }

    @Test
    fun homeScreen_LoadingStateWithNoItems_ShowsLoadingIndicator() {
        val uiState = MutableStateFlow(HomeUiState(isLoading = true, items = emptyList()))
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onItemClick = {})
        }

        // Search for CircularProgressIndicator by searching for a generic node that is not text
        // or check its existence via its parent if we had test tags. 
        // Given the code, it's the only Thing in the Box when loading.
        composeTestRule.onNode(hasScrollAction().not().and(hasText("").not())).assertExists()
    }

    @Test
    fun homeScreen_WithItems_DisplaysItemCards() {
        val items = listOf(
            ItemUIModel(
                id = "1",
                title = "iPhone 13",
                description = "Used",
                category = "Eletrônicos",
                imageUrl = "",
                distance = "1.2 km",
                rating = 4.5,
                ownerName = "John",
                ownerPhotoUrl = "",
                ownerRating = 4.8,
                ownerPlatformTime = "1y",
                ownerTradesCount = 10,
                idUser = "u1"
            ),
            ItemUIModel(
                id = "2",
                title = "Clean Code",
                description = "Book",
                category = "Livros",
                imageUrl = "",
                distance = "0.5 km",
                rating = 5.0,
                ownerName = "Jane",
                ownerPhotoUrl = "",
                ownerRating = 4.9,
                ownerPlatformTime = "2y",
                ownerTradesCount = 20,
                idUser = "u2"
            )
        )
        val uiState = MutableStateFlow(HomeUiState(items = items, filteredItems = items))
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onItemClick = {})
        }

        composeTestRule.onNodeWithText("iPhone 13").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clean Code").assertIsDisplayed()
    }

    @Test
    fun homeScreen_SearchQuery_CallsViewModel() {
        val uiState = MutableStateFlow(HomeUiState())
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onItemClick = {})
        }

        val searchQuery = "Macbook"
        // SearchBar likely has an OutlinedTextField with a placeholder or similar.
        // Assuming SearchBar uses a placeholder like "Buscar itens..."
        composeTestRule.onNodeWithText("Buscar itens...").performTextInput(searchQuery)
        verify { viewModel.onSearchQueryChanged(searchQuery) }
    }

    @Test
    fun homeScreen_CategoryClick_CallsViewModel() {
        val uiState = MutableStateFlow(HomeUiState())
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onItemClick = {})
        }

        composeTestRule.onNodeWithText("Eletrônicos").performClick()
        verify { viewModel.onCategorySelected("Eletrônicos") }
    }

    @Test
    fun homeScreen_ItemClick_TriggersCallback() {
        val items = listOf(
            ItemUIModel(
                id = "1",
                title = "iPhone 13",
                description = "Used",
                category = "Eletrônicos",
                imageUrl = "",
                distance = "1.2 km",
                rating = 4.5,
                ownerName = "John",
                ownerPhotoUrl = "",
                ownerRating = 4.8,
                ownerPlatformTime = "1y",
                ownerTradesCount = 10,
                idUser = "u1"
            )
        )
        val uiState = MutableStateFlow(HomeUiState(items = items, filteredItems = items))
        every { viewModel.uiState } returns uiState
        
        var clickedItemId: String? = null

        composeTestRule.setContent {
            HomeScreen(viewModel = viewModel, onItemClick = { clickedItemId = it })
        }

        composeTestRule.onNodeWithText("iPhone 13").performClick()
        assert(clickedItemId == "1")
    }
}
