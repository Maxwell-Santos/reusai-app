package com.example.reusai.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reusai.data.network.ItemRequest
import com.example.reusai.data.network.RetrofitClient
import com.example.reusai.data.network.StatusEnum
import com.example.reusai.data.network.TokenManager
import com.example.reusai.data.repository.ItemRepository
import com.example.reusai.ui.screens.CreateItemStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class CreateItemUiState(
    val currentStep: CreateItemStep = CreateItemStep.PHOTOS,
    val photos: List<Uri> = emptyList(),
    val isCompressing: Boolean = false,
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val idUser: String? = "",
    val isAvailableForTrade: Boolean = true,
    val isNeverUsed: Boolean = false,
    val isPublishing: Boolean = false,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false,
    val itemId: String? = null,
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false
)

class CreateItemViewModel(
    private val repository: ItemRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateItemUiState())
    val uiState: StateFlow<CreateItemUiState> = _uiState.asStateFlow()

    fun initialize(isEditMode: Boolean, itemId: String?) {
        _uiState.update { it.copy(isEditMode = isEditMode, itemId = itemId) }
        if (isEditMode && itemId != null) {
            loadItem(itemId)
        }
    }

    private fun loadItem(itemId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val item = RetrofitClient.instance.getItem(itemId)
                _uiState.update {
                    it.copy(
                        title = item.title,
                        category = item.category,
                        description = item.description,
                        isAvailableForTrade = item.availableToChange,
                        isNeverUsed = item.status == StatusEnum.NEW.name,
                        photos = listOf(Uri.parse(item.imageUrl)), // Assuming one image for now as per ItemResponse
                        isLoading = false,
                        idUser = item.idUser
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Erro ao carregar item: ${e.message}") }
            }
        }
    }

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
                val compressedFile = compressImage1MB(context, uri)
                _uiState.update { state ->
                    state.copy(
                        photos = state.photos + Uri.fromFile(compressedFile),
                        isCompressing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCompressing = false,
                        errorMessage = "Erro ao processar imagem"
                    )
                }
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

    fun publishItem(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true) }
            try {
                val state = _uiState.value
                if (state.photos.isEmpty()) {
                    _uiState.update { it.copy(errorMessage = "Selecione pelo menos uma foto") }
                    return@launch
                }

                // 1. Handle Image: if it's already a network URL (in edit mode), skip upload
                val photoUri = state.photos[0]
                val imageUrl = if (photoUri.toString().startsWith("http")) {
                    photoUri.toString()
                } else {
                    val file = File(photoUri.path ?: throw IOException("Caminho da imagem inválido"))
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    val uploadResponse = RetrofitClient.instance.uploadImage(body)
                    uploadResponse.url
                }

                val currentUserId = tokenManager.getUserSession()?.id ?: ""

                // 2. Create or Update the item
                val request = ItemRequest(
                    title = state.title,
                    category = state.category,
                    description = state.description,
                    availableToChange = state.isAvailableForTrade,
                    status = if (state.isNeverUsed) StatusEnum.NEW else StatusEnum.USED,
                    imageUrl = imageUrl,
                    idUser = currentUserId
                )

                if (state.isEditMode && state.itemId != null) {
                    RetrofitClient.instance.updateItem(state.itemId, request)
                } else {
                    RetrofitClient.instance.createItem(request)
                }

                // Success: clear state and navigate
                _uiState.update { CreateItemUiState() }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro ao processar: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isPublishing = false) }
            }
        }
    }

    fun deleteItem(onSuccess: () -> Unit) {
        val state = _uiState.value
        val itemId = state.itemId ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true) } // Using isPublishing as a generic loading state for actions
            try {
                RetrofitClient.instance.deleteItem(itemId)
                _uiState.update { CreateItemUiState().copy(isDeleted = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro ao excluir: ${e.message}", isPublishing = false) }
            }
        }
    }

    private suspend fun compressImage1MB(context: Context, uri: Uri): File =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val inputStream = resolver.openInputStream(uri)
                ?: throw IOException("Não foi possível abrir a imagem")

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val outputFile = File.createTempFile("compressed_", ".jpg", context.cacheDir)

            var quality = 100
            val maxSize = 1 * 1024 * 1024 // 1MB
            var stream: ByteArrayOutputStream

            do {
                stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                quality -= 5
            } while (stream.toByteArray().size > maxSize && quality > 10)

            FileOutputStream(outputFile).use {
                it.write(stream.toByteArray())
                it.flush()
            }

            outputFile
        }
}
