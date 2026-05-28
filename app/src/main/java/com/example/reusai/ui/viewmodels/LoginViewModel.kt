package com.example.reusai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reusai.data.network.LoginRequest
import com.example.reusai.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val tokenManager = com.example.reusai.data.network.RetrofitClient.getTokenManager()
        if (tokenManager?.isTokenValid() == true) {
            _isAuthenticated.value = true
        } else {
            tokenManager?.clearTokens()
            _isAuthenticated.value = false
        }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, emailError = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, passwordError = null) }
    }

    private fun validate(): Boolean {
        var isValid = true
        val currentState = _uiState.value

        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
        if (!emailRegex.matches(currentState.email)) {
            _uiState.update { it.copy(emailError = "E-mail inválido") }
            isValid = false
        }

        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Senha é obrigatória") }
            isValid = false
        }

        return isValid
    }

    fun login(onSuccess: () -> Unit) {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = _uiState.value
                val request = LoginRequest(
                    email = state.email,
                    password = state.password
                )

                val result = authRepository.login(request)
                
                if (result.isSuccess) {
                    _uiState.update { it.copy(isSuccess = true) }
                    onSuccess()
                } else {
                    _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Erro ao entrar") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro inesperado: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
