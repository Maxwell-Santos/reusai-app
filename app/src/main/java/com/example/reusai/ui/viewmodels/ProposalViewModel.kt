package com.example.reusai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reusai.data.network.ProposalResponse
import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.repository.ProposalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProposalUiState(
    val isLoading: Boolean = false,
    val receivedProposals: List<ProposalResponse> = emptyList(),
    val sentProposals: List<ProposalResponse> = emptyList(),
    val error: String? = null,
    val selectedTab: Int = 0 // 0 for Received, 1 for Sent
)

class ProposalViewModel(
    private val repository: ProposalRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProposalUiState())
    val uiState: StateFlow<ProposalUiState> = _uiState.asStateFlow()

    init {
        loadProposals()
    }

    fun loadProposals() {
        val currentUserId = tokenManager.getUserSession()?.id ?: ""
        if (currentUserId.isEmpty()) {
            _uiState.update { it.copy(isLoading = false, error = "Usuário não autenticado") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val received = repository.getProposalsReceived(currentUserId)
                val sent = repository.getProposalsSent(currentUserId)
                _uiState.update { it.copy(
                    receivedProposals = received,
                    sentProposals = sent,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao carregar propostas"
                ) }
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun acceptProposal(proposalId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.acceptProposal(proposalId)
                loadProposals() // Refresh list
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao aceitar proposta"
                ) }
            }
        }
    }

    fun rejectProposal(proposalId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.rejectProposal(proposalId)
                loadProposals() // Refresh list
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao recusar proposta"
                ) }
            }
        }
    }
}
