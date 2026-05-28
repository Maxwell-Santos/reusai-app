package com.example.reusai.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.reusai.ui.viewmodels.LoginViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.reusai.ui.viewmodels.LoginUiState

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<LoginViewModel>(relaxed = true)

    @Test
    fun loginScreen_InitialState_DisplaysAllElements() {
        val uiState = MutableStateFlow(LoginUiState())
        every { viewModel.uiState } returns uiState
        every { viewModel.isAuthenticated } returns MutableStateFlow(false)

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onSignUpClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        composeTestRule.onNodeWithText("E-mail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Senha").assertIsDisplayed()
        composeTestRule.onNodeWithText("Entrar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cadastre-se").assertIsDisplayed()
    }

    @Test
    fun loginScreen_InputEmailAndPassword_UpdatesViewModel() {
        val uiState = MutableStateFlow(LoginUiState())
        every { viewModel.uiState } returns uiState
        every { viewModel.isAuthenticated } returns MutableStateFlow(false)

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onSignUpClick = {},
                viewModel = viewModel
            )
        }

        val emailInput = "test@example.com"
        val passwordInput = "password123"

        composeTestRule.onNodeWithText("Digite seu e-mail").performTextInput(emailInput)
        verify { viewModel.onEmailChange(emailInput) }

        composeTestRule.onNodeWithText("Digite sua senha").performTextInput(passwordInput)
        verify { viewModel.onPasswordChange(passwordInput) }
    }

    @Test
    fun loginScreen_ClickLogin_CallsViewModelLogin() {
        val uiState = MutableStateFlow(LoginUiState())
        every { viewModel.uiState } returns uiState
        every { viewModel.isAuthenticated } returns MutableStateFlow(false)

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onSignUpClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Entrar").performClick()
        verify { viewModel.login(any()) }
    }

    @Test
    fun loginScreen_LoadingState_ShowsCircularProgress() {
        val uiState = MutableStateFlow(LoginUiState(isLoading = true))
        every { viewModel.uiState } returns uiState
        every { viewModel.isAuthenticated } returns MutableStateFlow(false)

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onSignUpClick = {},
                viewModel = viewModel
            )
        }

        // CircularProgressIndicator doesn't have a default test tag or text, 
        // but we can check if the "Entrar" text is NOT displayed since it's in the else branch
        composeTestRule.onNodeWithText("Entrar").assertDoesNotExist()
    }

    @Test
    fun loginScreen_ErrorMessage_ShowsDialog() {
        val errorMessage = "Invalid credentials"
        val uiState = MutableStateFlow(LoginUiState(errorMessage = errorMessage))
        every { viewModel.uiState } returns uiState
        every { viewModel.isAuthenticated } returns MutableStateFlow(false)

        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onSignUpClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithText("Erro de Login").assertIsDisplayed()
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("OK").performClick()
        verify { viewModel.clearError() }
    }
}
