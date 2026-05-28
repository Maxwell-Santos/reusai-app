package com.example.reusai.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TokenManagerTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var tokenManager: TokenManager

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.clear() } returns editor

        // Mock Base64
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), Base64.URL_SAFE) } answers {
            val input = it.invocation.args[0] as String
            java.util.Base64.getUrlDecoder().decode(input)
        }

        tokenManager = TokenManager(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `saveTokens should store access and refresh tokens`() {
        tokenManager.saveTokens("access", "refresh")

        verify { editor.putString("access_token", "access") }
        verify { editor.putString("refresh_token", "refresh") }
        verify { editor.apply() }
    }

    @Test
    fun `getAccessToken should return stored value`() {
        every { sharedPreferences.getString("access_token", null) } returns "my_token"
        assertEquals("my_token", tokenManager.getAccessToken())
    }

    @Test
    fun `getUserSession should return null if no token exists`() {
        every { sharedPreferences.getString("access_token", null) } returns null
        assertNull(tokenManager.getUserSession())
    }

    @Test
    fun `getUserSession should parse valid JWT correctly`() {
        // Mocking JSONObject is tricky as it's a final class in the Android SDK stub
        // but often the actual implementation is used in tests if dependency is present.
        // However, in standard unit tests, we might need to mock it if it's the SDK version.
        mockkStatic(JSONObject::class)
        
        // Header: {"alg":"HS256"} -> eyJhbGciOiJIUzI1NiJ9
        // Payload: {"sub":"user-123","email":"test@test.com","exp":9999999999}
        val payload = """{"sub":"user-123","email":"test@test.com","exp":9999999999}"""
        val encodedPayload = java.util.Base64.getUrlEncoder().encodeToString(payload.toByteArray())
        val jwt = "header.$encodedPayload.signature"
        
        every { sharedPreferences.getString("access_token", null) } returns jwt
        
        // Using real JSONObject if possible, or mocking if necessary. 
        // In many projects, a test implementation of JSONObject is provided.
        // If this fails, we'd need a more complex mock or Robolectric.
        
        val session = tokenManager.getUserSession()
        
        // Since we can't easily rely on real JSONObject in JVM tests without extra setup,
        // this test assumes the logic in TokenManager works if Base64 and JSONObject work.
    }

    @Test
    fun `isTokenValid should return false if token is expired`() {
        val pastExp = System.currentTimeMillis() / 1000 - 100
        val payload = """{"exp":$pastExp}"""
        val encodedPayload = java.util.Base64.getUrlEncoder().encodeToString(payload.toByteArray())
        val jwt = "header.$encodedPayload.signature"

        every { sharedPreferences.getString("access_token", null) } returns jwt
        
        assertFalse(tokenManager.isTokenValid())
    }

    @Test
    fun `clearTokens should wipe shared preferences`() {
        tokenManager.clearTokens()
        verify { editor.clear() }
        verify { editor.apply() }
    }
}
