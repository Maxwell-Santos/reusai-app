package com.example.reusai.data.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var tokenManager: TokenManager
    private lateinit var interceptor: AuthInterceptor
    private lateinit var chain: Interceptor.Chain
    private lateinit var request: Request
    private lateinit var response: Response

    @Before
    fun setup() {
        tokenManager = mockk(relaxed = true)
        interceptor = AuthInterceptor(tokenManager)
        chain = mockk(relaxed = true)
        request = Request.Builder().url("http://api.com/item").build()
        response = mockk(relaxed = true)
    }

    @Test
    fun `intercept should add Authorization header when token is available`() {
        every { tokenManager.getAccessToken() } returns "valid_token"
        every { chain.request() } returns request
        every { chain.proceed(any()) } returns response

        interceptor.intercept(chain)

        verify {
            chain.proceed(withArg {
                assertEquals("Bearer valid_token", it.header("Authorization"))
            })
        }
    }

    @Test
    fun `intercept should not add Authorization header for signin path`() {
        val signinRequest = Request.Builder().url("http://api.com/auth/signin").build()
        every { chain.request() } returns signinRequest
        every { chain.proceed(any()) } returns response

        interceptor.intercept(chain)

        verify {
            chain.proceed(withArg {
                assertNull(it.header("Authorization"))
            })
        }
    }

    @Test
    fun `intercept should clear tokens on 401 response`() {
        every { chain.request() } returns request
        every { chain.proceed(any()) } returns response
        every { response.code } returns 401

        interceptor.intercept(chain)

        verify { tokenManager.clearTokens() }
    }
}
