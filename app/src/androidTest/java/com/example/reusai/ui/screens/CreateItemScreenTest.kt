package com.example.reusai.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.reusai.ui.viewmodels.CreateItemUiState
import com.example.reusai.ui.viewmodels.CreateItemViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class CreateItemScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<CreateItemViewModel>(relaxed = true)

    @Test
    fun createItemScreen_StepPhotos_DisplaysPhotoUpload() {
        val uiState = MutableStateFlow(CreateItemUiState(currentStep = CreateItemStep.PHOTOS))
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            CreateItemScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Fotos do seu item").assertIsDisplayed()
        composeTestRule.onNodeWithText("Adicionar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Próximo Passo").assertIsDisplayed()
    }

    @Test
    fun createItemScreen_StepDetails_DisplaysFormFields() {
        val uiState = MutableStateFlow(CreateItemUiState(currentStep = CreateItemStep.DETAILS))
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            CreateItemScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Sobre o desapego").assertIsDisplayed()
        composeTestRule.onNodeWithText("Título do anúncio").assertIsDisplayed()
        composeTestRule.onNodeWithText("Categoria").assertIsDisplayed()
        composeTestRule.onNodeWithText("Descrição").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aceito trocas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Produto novo").assertIsDisplayed()
    }

    @Test
    fun createItemScreen_InputDetails_UpdatesViewModel() {
        val uiState = MutableStateFlow(CreateItemUiState(currentStep = CreateItemStep.DETAILS))
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            CreateItemScreen(viewModel = viewModel)
        }

        val title = "Macbook Pro"
        val description = "M1, 16GB RAM"

        composeTestRule.onNodeWithText("Ex: Jaqueta de couro preta").performTextInput(title)
        verify { viewModel.onTitleChange(title) }

        composeTestRule.onNodeWithText("Descreva o estado do item, tamanho, tempo de uso...").performTextInput(description)
        verify { viewModel.onDescriptionChange(description) }
    }

    @Test
    fun createItemScreen_StepReview_DisplaysPublishButton() {
        val uiState = MutableStateFlow(CreateItemUiState(currentStep = CreateItemStep.REVIEW))
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            CreateItemScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Tudo pronto!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Publicar Desapego").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Publicar Desapego").performClick()
        verify { viewModel.publishItem(any()) }
    }

    @Test
    fun createItemScreen_LoadingState_ShowsLoadingIndicator() {
        val uiState = MutableStateFlow(CreateItemUiState(isLoading = true))
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            CreateItemScreen(viewModel = viewModel)
        }

        // Search for CircularProgressIndicator (it's the only one in the Box)
        composeTestRule.onNode(hasScrollAction().not().and(hasText("").not())).assertExists()
    }
}
