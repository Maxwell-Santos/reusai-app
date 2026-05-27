package com.example.reusai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reusai.data.network.ProposalRequest
import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.repository.AuthRepository
import com.example.reusai.data.repository.ItemRepository
import com.example.reusai.data.repository.ItemUIModel
import com.example.reusai.data.repository.ProposalRepository
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
    private val tokenManager: TokenManager,
    private val proposalRepository: ProposalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val currentUserId = tokenManager.getUserSession()?.id ?: ""

    fun loadItem(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val item = repository.getItemById(itemId)
                if (item != null) {
                    _uiState.update { it.copy(item = item) }
                    
                    if (item.idUser.isNotEmpty()) {
                        val userResult = authRepository.getUser(item.idUser)
                        if (userResult.isSuccess) {
                            userResult.getOrNull()?.let { user ->
                                _uiState.update { currentState ->
                                    currentState.copy(
                                        item = currentState.item?.copy(
                                            ownerName = user.username,
                                            ownerPhotoUrl = user.photoUrl ?: ""
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    _uiState.update { it.copy(error = "Item não encontrado") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Erro ao carregar detalhes do item") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
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
        val currentState = _uiState.value
        val targetItem = currentState.item
        val myItem = currentState.selectedUserItem

        if (targetItem == null || myItem == null) {
            _uiState.update { it.copy(error = "Selecione um item para oferecer") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                proposalRepository.createProposal(
                    ProposalRequest(
                        idUserFrom = currentUserId,
                        idUserTo = targetItem.idUser,
                        idItemFrom = myItem.id,
                        idItemTo = targetItem.id
                    )
                )
                _uiState.update { it.copy(isTradeSuccess = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao enviar proposta"
                ) }
            }
        }
    }

    fun resetTradeSuccess() {
        _uiState.update { it.copy(isTradeSuccess = false) }
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
    }
}
