package com.example.reusai.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import org.json.JSONObject

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)

    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    fun getUserSession(): UserSession? {
        val token = getAccessToken() ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val json = JSONObject(payload)
            UserSession(
                id = json.optString("sub", ""),
                email = json.optString("email", "") // Assumindo que o email está no claim 'email' ou similar
            )
        } catch (e: Exception) {
            null
        }
    }

    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}
