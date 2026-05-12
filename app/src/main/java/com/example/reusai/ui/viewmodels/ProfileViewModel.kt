package com.example.reusai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reusai.data.network.ItemResponse
import com.example.reusai.data.network.RetrofitClient
import com.example.reusai.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isLogoutSuccess: Boolean = false,
    val items: List<ItemResponse> = emptyList(),
    val errorMessage: String? = null,
    // Mocked user data as per requirements
    val userName: String = "Você (Mariana)",
    val location: String = "São Paulo, SP",
    val profilePhotoUrl: String = "https://i.pravatar.cc/150?u=mariana",
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
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        fetchItems()
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = authRepository.logout()
            if (result.isSuccess) {
                _uiState.update { it.copy(isLogoutSuccess = true, isLoading = false) }
            } else {
                _uiState.update { it.copy(errorMessage = "Erro ao sair", isLoading = false) }
            }
        }
    }

    fun fetchItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val response = RetrofitClient.instance.getItems()
                _uiState.update { it.copy(items = response, activeItems = response.size) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro ao carregar itens: ${e.localizedMessage}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
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
