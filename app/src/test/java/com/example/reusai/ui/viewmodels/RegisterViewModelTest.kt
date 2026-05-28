package com.example.reusai.ui.viewmodels

import android.net.Uri
import app.cash.turbine.test
import com.example.reusai.data.network.ReusaiApiService
import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.network.UploadImageResponse
import com.example.reusai.data.network.UserResponse
import com.example.reusai.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
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
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var apiService: ReusaiApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        apiService = mockk()
        tokenManager = mockk(relaxed = true)

        // Mock RetrofitClient
        mockkObject(com.example.reusai.data.network.RetrofitClient)
        every { com.example.reusai.data.network.RetrofitClient.getTokenManager() } returns tokenManager
        every { com.example.reusai.data.network.RetrofitClient.instance } returns apiService
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state should be empty`() = runTest {
        viewModel = RegisterViewModel(authRepository)
        val state = viewModel.uiState.value
        assertEquals("", state.username)
        assertEquals("", state.cep)
        assertEquals("", state.email)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `onCepChange should format CEP correctly`() = runTest {
        viewModel = RegisterViewModel(authRepository)
        
        viewModel.onCepChange("12345678")
        assertEquals("12345-678", viewModel.uiState.value.cep)

        viewModel.onCepChange("123")
        assertEquals("123", viewModel.uiState.value.cep)
    }

    @Test
    fun `register should fail if validation fails`() = runTest {
        viewModel = RegisterViewModel(authRepository)
        // Leave fields empty
        
        viewModel.register {}
        
        val state = viewModel.uiState.value
        assertTrue(state.nameError != null)
        assertTrue(state.cepError != null)
        assertTrue(state.emailError != null)
        assertTrue(state.passwordError != null)
    }

    @Test
    fun `register success without photo should update state and callback`() = runTest {
        val userResponse = UserResponse("id", "user", "test@test.com")
        coEvery { authRepository.register(any()) } returns Result.success(userResponse)

        viewModel = RegisterViewModel(authRepository)
        viewModel.onNameChange("John Doe")
        viewModel.onCepChange("12345678")
        viewModel.onEmailChange("john@example.com")
        viewModel.onPasswordChange("123456")

        var successCalled = false
        
        viewModel.uiState.test {
            awaitItem() // Initial
            
            viewModel.register { successCalled = true }
            
            assertTrue(awaitItem().isLoading)
            
            // Aguarda a emissão do sucesso (isLoading continua true aqui)
            val stateWithSuccess = awaitItem()
            assertTrue(stateWithSuccess.isSuccess)
            
            // Aguarda a emissão final do finally (isLoading = false)
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertTrue(finalState.isSuccess)
            assertTrue(successCalled)
        }
    }

    @Test
    fun `register failure should update errorMessage`() = runTest {
        val errorMsg = "Email already exists"
        coEvery { authRepository.register(any()) } returns Result.failure(Exception(errorMsg))

        viewModel = RegisterViewModel(authRepository)
        viewModel.onNameChange("John Doe")
        viewModel.onCepChange("12345678")
        viewModel.onEmailChange("john@example.com")
        viewModel.onPasswordChange("123456")

        viewModel.register {}
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(errorMsg, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `checkAuthStatus should update isAuthenticated`() = runTest {
        every { tokenManager.getAccessToken() } returns "valid_token"
        
        viewModel = RegisterViewModel(authRepository)
        
        assertTrue(viewModel.isAuthenticated.value)
    }

    @Test
    fun `clearError should reset error message`() = runTest {
        viewModel = RegisterViewModel(authRepository)
        // Simulate error
        coEvery { authRepository.register(any()) } returns Result.failure(Exception("Error"))
        viewModel.onNameChange("A")
        viewModel.onCepChange("12345678")
        viewModel.onEmailChange("a@a.com")
        viewModel.onPasswordChange("123456")
        viewModel.register {}
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.errorMessage != null)
        
        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
