package com.example.reusai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.repository.ItemRepository
import com.example.reusai.data.repository.ItemUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailsUiState(
    val item: ItemUIModel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val userItems: List<ItemUIModel> = emptyList(),
    val selectedUserItem: ItemUIModel? = null,
    val isTradeSuccess: Boolean = false
)

class DetailsViewModel(
    private val repository: ItemRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    fun loadItem(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val item = repository.getItemById(itemId)
                _uiState.value = _uiState.value.copy(
                    item = item,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao carregar detalhes do item"
                )
            }
        }
    }

    fun loadUserItems() {
        viewModelScope.launch {
            val userSession = tokenManager.getUserSession()
            if (userSession != null) {
                _uiState.update { it.copy(isLoading = true) }
                try {
                    val userItems = repository.getItemsByUser(userSession.id)
                    _uiState.update { it.copy(userItems = userItems, isLoading = false) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            } else {
                _uiState.update { it.copy(error = "Usuário não autenticado") }
            }
        }
    }

    fun selectUserItem(item: ItemUIModel) {
        _uiState.update { it.copy(selectedUserItem = item) }
    }

    fun confirmTrade() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Simular chamada de API
            kotlinx.coroutines.delay(1000)
            _uiState.update { it.copy(isTradeSuccess = true, isLoading = false) }
        }
    }

    fun resetTradeSuccess() {
        _uiState.update { it.copy(isTradeSuccess = false) }
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
    }
}

