package com.example.reusai.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.reusai.ui.screens.CreateItemStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

data class CreateItemUiState(
    val currentStep: CreateItemStep = CreateItemStep.PHOTOS,
    val photos: List<Uri> = emptyList(),
    val isCompressing: Boolean = false,
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val isAvailableForTrade: Boolean = true,
    val isNeverUsed: Boolean = false,
    val isPublishing: Boolean = false,
    val errorMessage: String? = null
)

class CreateItemViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateItemUiState())
    val uiState: StateFlow<CreateItemUiState> = _uiState.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onCategoryChange(newCategory: String) {
        _uiState.update { it.copy(category = newCategory) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onTradeToggle(isEnabled: Boolean) {
        _uiState.update { it.copy(isAvailableForTrade = isEnabled) }
    }

    fun onNeverUsedToggle(isEnabled: Boolean) {
        _uiState.update { it.copy(isNeverUsed = isEnabled) }
    }

    fun nextStep() {
        _uiState.update { state ->
            val nextStep = when (state.currentStep) {
                CreateItemStep.PHOTOS -> CreateItemStep.DETAILS
                CreateItemStep.DETAILS -> CreateItemStep.REVIEW
                CreateItemStep.REVIEW -> CreateItemStep.REVIEW
            }
            state.copy(currentStep = nextStep)
        }
    }

    fun previousStep(onNavigateBack: () -> Unit) {
        val state = _uiState.value
        when (state.currentStep) {
            CreateItemStep.PHOTOS -> onNavigateBack()
            CreateItemStep.DETAILS -> _uiState.update { it.copy(currentStep = CreateItemStep.PHOTOS) }
            CreateItemStep.REVIEW -> _uiState.update { it.copy(currentStep = CreateItemStep.DETAILS) }
        }
    }

    fun addPhoto(context: Context, uri: Uri) {
        if (_uiState.value.photos.size >= 4) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCompressing = true) }
            try {
                val compressedFile = compressImage(context, uri)
                _uiState.update { state ->
                    state.copy(
                        photos = state.photos + Uri.fromFile(compressedFile),
                        isCompressing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isCompressing = false, errorMessage = "Erro ao processar imagem") }
            }
        }
    }

    fun removePhoto(uri: Uri) {
        _uiState.update { state ->
            state.copy(photos = state.photos - uri)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun compressImage(context: Context, uri: Uri): File = withContext(Dispatchers.IO) {
        val outputFile = File.createTempFile("compressed_", ".jpg", context.cacheDir).apply {
            deleteOnExit()
        }

        try {
            Glide.with(context)
                .asFile()
                .load(uri)
                .apply(
                    RequestOptions()
                        .override(1024, 768)
                        .encodeQuality(70)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .format(DecodeFormat.PREFER_RGB_565)
                )
                .submit()
                .get()
        } catch (e: Exception) {
            throw IOException("Falha ao comprimir imagem: ${e.message}")
        }
    }
}
