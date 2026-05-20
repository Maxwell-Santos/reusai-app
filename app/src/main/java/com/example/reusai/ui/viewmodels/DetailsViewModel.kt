package com.example.reusai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class DetailsViewModel(private val repository: ItemRepository) : ViewModel() {

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
            // Mocking user items for now as well
            val mockUserItems = listOf(
                ItemUIModel(
                    id = "u1",
                    title = "Teclado Mecânico RGB",
                    category = "Eletrônicos",
                    description = "Teclado mecânico com switches azuis, pouco tempo de uso.",
                    imageUrl = "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?q=80&w=500",
                    distance = "Sua localização",
                    rating = 5.0,
                    ownerName = "Você (Mariana)",
                    ownerPhotoUrl = "https://i.pravatar.cc/150?u=mariana",
                    ownerRating = 4.9,
                    ownerPlatformTime = "Na plataforma há 1 ano",
                    ownerTradesCount = 23
                ),
                ItemUIModel(
                    id = "u2",
                    title = "Mouse Gamer 12000 DPI",
                    category = "Eletrônicos",
                    description = "Mouse ergonômico com pesos ajustáveis.",
                    imageUrl = "https://images.unsplash.com/photo-1527661591475-527312dd65f5?q=80&w=500",
                    distance = "Sua localização",
                    rating = 4.7,
                    ownerName = "Você (Mariana)",
                    ownerPhotoUrl = "https://i.pravatar.cc/150?u=mariana",
                    ownerRating = 4.9,
                    ownerPlatformTime = "Na plataforma há 1 ano",
                    ownerTradesCount = 23
                )
            )
            _uiState.update { it.copy(userItems = mockUserItems) }
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

