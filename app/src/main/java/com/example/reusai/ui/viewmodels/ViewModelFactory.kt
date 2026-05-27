package com.example.reusai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.repository.AuthRepository
import com.example.reusai.data.repository.ItemRepository
import com.example.reusai.data.repository.ProposalRepository

class ViewModelFactory(
    private val itemRepository: ItemRepository,
    private val proposalRepository: ProposalRepository,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(itemRepository) as T
            }
            modelClass.isAssignableFrom(DetailsViewModel::class.java) -> {
                DetailsViewModel(itemRepository, tokenManager, proposalRepository, authRepository) as T
            }
            modelClass.isAssignableFrom(ProposalViewModel::class.java) -> {
                ProposalViewModel(proposalRepository, tokenManager) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(itemRepository, tokenManager, authRepository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(CreateItemViewModel::class.java) -> {
                CreateItemViewModel(itemRepository, tokenManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
