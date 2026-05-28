package com.example.reusai.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.reusai.ui.viewmodels.RegisterUiState
import com.example.reusai.ui.viewmodels.RegisterViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<RegisterViewModel>(relaxed = true)

    @Test
    fun registerScreen_InitialState_DisplaysAllElements() {
        val uiState = MutableStateFlow(RegisterUiState())
        every { viewModel.uiState } returns uiState
        every { viewModel.isAuthenticated } returns MutableStateFlow(false)

        composeTestRule.setContent {
            RegisterScreen(
                onNavigateBack = {},
                onLoginClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Criar Conta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nome").assertIsDisplayed()
        composeTestRule.onNodeWithText("CEP").assertIsDisplayed()
        composeTestRule.onNodeWithText("E-mail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Senha").assertIsDisplayed()
        composeTestRule.onNodeWithText("FOTO (OPCIONAL)").assertIsDisplayed()
    }

    @Test
    fun registerScreen_InputFields_UpdatesViewModel() {
        val uiState = MutableStateFlow(RegisterUiState())
        every { viewModel.uiState } returns uiState
        every { viewModel.isAuthenticated } returns MutableStateFlow(false)

        composeTestRule.setContent {
            RegisterScreen(
                onNavigateBack = {},
                onLoginClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Digite seu nome completo").performTextInput("John Doe")
        verify { viewModel.onNameChange("John Doe") }

        composeTestRule.onNodeWithText("00000-000").performTextInput("12345678")
        verify { viewModel.onCepChange("12345678") }
    }

    @Test
    fun registerScreen_ClickRegister_CallsViewModelRegister() {
        val uiState = MutableStateFlow(RegisterUiState())
        every { viewModel.uiState } returns uiState
        every { viewModel.isAuthenticated } returns MutableStateFlow(false)

        composeTestRule.setContent {
            RegisterScreen(
                onNavigateBack = {},
                onLoginClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Criar Conta", useUnmergedTree = true).performClick()
        verify { viewModel.register(any()) }
    }
}
