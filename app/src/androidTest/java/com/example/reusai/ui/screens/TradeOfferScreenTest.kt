package com.example.reusai.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.reusai.data.repository.ItemUIModel
import com.example.reusai.ui.viewmodels.DetailsUiState
import com.example.reusai.ui.viewmodels.DetailsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class TradeOfferScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<DetailsViewModel>(relaxed = true)

    private val mockTargetItem = ItemUIModel(
        id = "1",
        title = "Target Item",
        category = "Electronics",
        description = "Description",
        imageUrl = "",
        distance = "1km",
        rating = 4.0,
        ownerName = "Owner",
        ownerPhotoUrl = "",
        ownerRating = 4.5,
        ownerPlatformTime = "1y",
        ownerTradesCount = 5
    )

    private val mockUserItem = ItemUIModel(
        id = "2",
        title = "My Item",
        category = "Books",
        description = "My Description",
        imageUrl = "",
        distance = "0km",
        rating = 5.0,
        ownerName = "Me",
        ownerPhotoUrl = "",
        ownerRating = 5.0,
        ownerPlatformTime = "2y",
        ownerTradesCount = 10
    )

    @Test
    fun tradeOfferScreen_InitialState_DisplaysItems() {
        val uiState = MutableStateFlow(
            DetailsUiState(
                item = mockTargetItem,
                userItems = listOf(mockUserItem)
            )
        )
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            TradeOfferScreen(
                itemId = "1",
                viewModel = viewModel,
                onNavigateBack = {},
                onTradeSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Oferecer Troca").assertIsDisplayed()
        composeTestRule.onNodeWithText("Target Item").assertIsDisplayed()
        composeTestRule.onNodeWithText("My Item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirmar Proposta").assertIsNotEnabled()
    }

    @Test
    fun tradeOfferScreen_SelectItem_EnablesConfirmButton() {
        val uiState = MutableStateFlow(
            DetailsUiState(
                item = mockTargetItem,
                userItems = listOf(mockUserItem),
                selectedUserItem = null
            )
        )
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            TradeOfferScreen(
                itemId = "1",
                viewModel = viewModel,
                onNavigateBack = {},
                onTradeSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("My Item").performClick()
        verify { viewModel.selectUserItem(mockUserItem) }

        // Update state to selected
        uiState.value = uiState.value.copy(selectedUserItem = mockUserItem)
        
        composeTestRule.onNodeWithText("Confirmar Proposta").assertIsEnabled()
    }

    @Test
    fun tradeOfferScreen_ConfirmTrade_CallsViewModel() {
        val uiState = MutableStateFlow(
            DetailsUiState(
                item = mockTargetItem,
                userItems = listOf(mockUserItem),
                selectedUserItem = mockUserItem
            )
        )
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            TradeOfferScreen(
                itemId = "1",
                viewModel = viewModel,
                onNavigateBack = {},
                onTradeSuccess = {}
            )
        }

        composeTestRule.onNodeWithText("Confirmar Proposta").performClick()
        verify { viewModel.confirmTrade() }
    }
}
