package com.example.reusai.data.repository

import com.example.reusai.data.network.AuthResponse
import com.example.reusai.data.network.LoginRequest
import com.example.reusai.data.network.RetrofitClient
import com.example.reusai.data.network.ReusaiApiService
import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.network.UserRequest
import com.example.reusai.data.network.UserResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private lateinit var apiService: ReusaiApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        apiService = mockk()
        tokenManager = mockk(relaxed = true)
        
        // Mock RetrofitClient
        mockkObject(RetrofitClient)
        every { RetrofitClient.instance } returns apiService
        every { RetrofitClient.getTokenManager() } returns tokenManager
        
        repository = AuthRepository(apiService)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `login success should save tokens and return response`() = runTest {
        val loginRequest = LoginRequest("test@example.com", "password")
        val authResponse = AuthResponse(
            username = "testuser",
            authenticated = true,
            created = Date(),
            expiration = Date(),
            accessToken = "access",
            refreshToken = "refresh"
        )
        coEvery { apiService.login(loginRequest) } returns authResponse

        val result = repository.login(loginRequest)

        assertTrue(result.isSuccess)
        assertEquals(authResponse, result.getOrNull())
        verify { tokenManager.saveTokens("access", "refresh") }
    }

    @Test
    fun `login failure should return failure result`() = runTest {
        val exception = Exception("Invalid credentials")
        coEvery { apiService.login(any()) } throws exception

        val result = repository.login(LoginRequest("test@example.com", "wrong"))

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `register success should return user response`() = runTest {
        val userRequest = UserRequest("user", "12345678", "test@test.com", "pass")
        val userResponse = UserResponse("id", "user", "test@test.com")
        coEvery { apiService.createUser(userRequest) } returns userResponse

        val result = repository.register(userRequest)

        assertTrue(result.isSuccess)
        assertEquals(userResponse, result.getOrNull())
    }

    @Test
    fun `logout should clear tokens even if api fails`() = runTest {
        coEvery { apiService.logout() } throws Exception("API Error")

        val result = repository.logout()

        assertTrue(result.isSuccess)
        verify { tokenManager.clearTokens() }
    }

    @Test
    fun `refreshToken success should update tokens`() = runTest {
        val authResponse = AuthResponse(
            username = "testuser",
            authenticated = true,
            created = Date(),
            expiration = Date(),
            accessToken = "newAccess",
            refreshToken = "newRefresh"
        )
        coEvery { apiService.refreshToken("testuser") } returns authResponse

        val result = repository.refreshToken("testuser")

        assertTrue(result.isSuccess)
        verify { tokenManager.saveTokens("newAccess", "newRefresh") }
    }

    @Test
    fun `getUser should return hardcoded user based on ID`() = runTest {
        // AuthRepository currently has hardcoded logic for getUser
        val userId = "2d7211ca-6687-4f1f-a224-1140ac87a827"
        
        val result = repository.getUser(userId)
        
        assertTrue(result.isSuccess)
        assertEquals("Leonardo", result.getOrNull()?.username)
    }
}
