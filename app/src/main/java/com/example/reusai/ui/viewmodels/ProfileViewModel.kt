package com.example.reusai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.repository.ItemRepository
import com.example.reusai.data.repository.ItemUIModel
import com.example.reusai.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isLogoutSuccess: Boolean = false,
    val items: List<ItemUIModel> = emptyList(),
    val errorMessage: String? = null,
    // Mocked user data as per requirements
    val userName: String = "",
    val location: String = "São Paulo, SP",
    val profilePhotoUrl: String = "https://www.vozdobico.com.br/opiniao-ideias-e-debates/criar-perfil-fake-e-crime/attachment/perfil-sem-foto-fake/",
    val completedSwaps: Int = 23,
    val activeItems: Int = 4,
    val reputation: Double = 4.9,
    val recentReviews: List<ReviewUiModel> = listOf(
        ReviewUiModel(
            stars = 5,
            comment = "Ótima troca, item em perfeito estado! Muito educada e pontual.",
            reviewerName = "João P.",
            timeAgo = "Há 2 dias"
        )
    )
)

data class ReviewUiModel(
    val stars: Int,
    val comment: String,
    val reviewerName: String,
    val timeAgo: String
)

class ProfileViewModel(
    private val repository: ItemRepository,
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadUserItems()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val userSession = tokenManager.getUserSession()
            if (userSession != null) {
                authRepository.getUser(userSession.id).onSuccess { user ->
                    _uiState.update { it.copy(
                        userName = user.username,
                        profilePhotoUrl = user.photoUrl ?: ""
                    ) }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.logout()
            if (result.isSuccess) {
                tokenManager.clearTokens()
                _uiState.update { it.copy(isLogoutSuccess = true, isLoading = false) }
            } else {
                _uiState.update { it.copy(errorMessage = "Erro ao sair", isLoading = false) }
            }
        }
    }

    fun loadUserItems() {
        viewModelScope.launch {
            val userSession = tokenManager.getUserSession()
            if (userSession != null) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                try {
                    val userItems = repository.getItemsByUser(userSession.id)
                    _uiState.update { it.copy(items = userItems, activeItems = userItems.size) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMessage = "Erro ao carregar itens: ${e.localizedMessage}") }
                } finally {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Usuário não autenticado") }
            }
        }
    }

    fun deleteItem(itemId: String) {
        // Stubbed for now
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
