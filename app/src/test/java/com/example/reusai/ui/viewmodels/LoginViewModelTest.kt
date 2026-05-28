package com.example.reusai.ui.viewmodels

import app.cash.turbine.test
import com.example.reusai.data.network.AuthResponse
import com.example.reusai.data.network.RetrofitClient
import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
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
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        
        // Mock RetrofitClient como Object (Singleton)
        mockkObject(RetrofitClient)
        every { RetrofitClient.instance } returns mockk(relaxed = true)
        every { RetrofitClient.getTokenManager() } returns tokenManager
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state should have empty fields and no errors`() = runTest {
        viewModel = LoginViewModel(authRepository)
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.errorMessage)
        assertNull(state.emailError)
        assertNull(state.passwordError)
    }

    @Test
    fun `onEmailChange should update email and clear email error`() = runTest {
        viewModel = LoginViewModel(authRepository)
        viewModel.onEmailChange("test@example.com")
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `onPasswordChange should update password and clear password error`() = runTest {
        viewModel = LoginViewModel(authRepository)
        viewModel.onPasswordChange("password123")
        assertEquals("password123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `login should fail when email is invalid`() = runTest {
        viewModel = LoginViewModel(authRepository)
        viewModel.onEmailChange("invalid-email")
        viewModel.onPasswordChange("password123")

        viewModel.login {}

        assertTrue(viewModel.uiState.value.emailError != null)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login should fail when password is blank`() = runTest {
        viewModel = LoginViewModel(authRepository)
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("")

        viewModel.login {}

        assertTrue(viewModel.uiState.value.passwordError != null)
    }

    @Test
    fun `login success should update state and trigger callback`() = runTest {
        val authResponse = AuthResponse(
            username = "testuser",
            authenticated = true,
            created = Date(),
            expiration = Date(),
            accessToken = "token",
            refreshToken = "refresh"
        )
        coEvery { authRepository.login(any()) } returns Result.success(authResponse)

        viewModel = LoginViewModel(authRepository)
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")

        var callbackCalled = false
        
        viewModel.uiState.test {
            awaitItem() // Skip current state (already has email/password)
            
            viewModel.login { callbackCalled = true }
            
            // Loading state
            assertTrue(awaitItem().isLoading)
            
            // Success state (isLoading still true)
            val successState = awaitItem()
            assertTrue(successState.isSuccess)

            // Final state (isLoading false)
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertTrue(finalState.isSuccess)
            assertTrue(callbackCalled)
        }
    }

    @Test
    fun `login failure should update state with error message`() = runTest {
        val errorMessage = "Invalid credentials"
        coEvery { authRepository.login(any()) } returns Result.failure(Exception(errorMessage))

        viewModel = LoginViewModel(authRepository)
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.uiState.test {
            awaitItem() // Skip current state
            
            viewModel.login {}
            
            assertTrue(awaitItem().isLoading)
            
            // Error state (isLoading still true)
            val stateWithError = awaitItem()
            assertEquals(errorMessage, stateWithError.errorMessage)

            // Final state (isLoading false)
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertEquals(errorMessage, finalState.errorMessage)
        }
    }

    @Test
    fun `checkAuthStatus should set isAuthenticated to true when token is valid`() = runTest {
        every { tokenManager.isTokenValid() } returns true
        
        viewModel = LoginViewModel(authRepository)
        
        assertTrue(viewModel.isAuthenticated.value)
    }

    @Test
    fun `checkAuthStatus should set isAuthenticated to false and clear tokens when token is invalid`() = runTest {
        every { tokenManager.isTokenValid() } returns false
        
        viewModel = LoginViewModel(authRepository)
        
        assertFalse(viewModel.isAuthenticated.value)
        verify { tokenManager.clearTokens() }
    }
}
