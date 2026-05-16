package com.example.reusai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reusai.data.repository.ItemRepository
import com.example.reusai.data.repository.ItemUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val items: List<ItemUIModel> = emptyList(),
    val filteredItems: List<ItemUIModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String = "Todos",
    val searchQuery: String = ""
)

class HomeViewModel(private val repository: ItemRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val items = repository.getExploreItems()
                _uiState.value = _uiState.value.copy(
                    items = items,
                    filteredItems = items,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao carregar itens"
                )
            }
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        filterItems()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterItems()
    }

    private fun filterItems() {
        val currentState = _uiState.value
        val filtered = currentState.items.filter { item ->
            val matchesCategory = currentState.selectedCategory == "Todos" || item.category == currentState.selectedCategory
            val matchesSearch = item.title.contains(currentState.searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        _uiState.value = _uiState.value.copy(filteredItems = filtered)
    }
}
