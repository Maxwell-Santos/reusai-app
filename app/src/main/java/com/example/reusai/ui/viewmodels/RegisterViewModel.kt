package com.example.reusai.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reusai.data.network.RetrofitClient
import com.example.reusai.data.network.UserRequest
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

data class RegisterUiState(
    val name: String = "",
    val cep: String = "",
    val email: String = "",
    val password: String = "",
    val profilePhotoUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val nameError: String? = null,
    val cepError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, nameError = null) }
    }

    fun onCepChange(newCep: String) {
        // Simple CEP formatting (00000-000)
        val digits = newCep.filter { it.isDigit() }.take(8)
        val formatted = if (digits.length > 5) {
            "${digits.substring(0, 5)}-${digits.substring(5)}"
        } else {
            digits
        }
        _uiState.update { it.copy(cep = formatted, cepError = null) }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, emailError = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, passwordError = null) }
    }

    fun onPhotoSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val compressedFile = compressImage1MB(context, uri)
                _uiState.update { it.copy(profilePhotoUri = Uri.fromFile(compressedFile)) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro ao processar foto") }
            }
        }
    }

    private fun validate(): Boolean {
        var isValid = true
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Nome é obrigatório") }
            isValid = false
        }

        if (currentState.cep.replace("-", "").length != 8) {
            _uiState.update { it.copy(cepError = "CEP inválido") }
            isValid = false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.update { it.copy(emailError = "E-mail inválido") }
            isValid = false
        }

        if (currentState.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Senha deve ter pelo menos 6 caracteres") }
            isValid = false
        }

        return isValid
    }

    fun register(onSuccess: () -> Unit) {
        if (!validate()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val state = _uiState.value
                var profilePhotoUrl: String? = null

                // 1. Upload photo if exists
                state.profilePhotoUri?.let { uri ->
                    val file = File(uri.path ?: throw IOException("Caminho da imagem inválido"))
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    val uploadResponse = RetrofitClient.instance.uploadImage(body)
                    profilePhotoUrl = uploadResponse.url
                }

                // 2. Create user
                val request = UserRequest(
                    name = state.name,
                    cep = state.cep,
                    email = state.email,
                    password = state.password,
                    profilePhotoUrl = profilePhotoUrl
                )

                RetrofitClient.instance.createUser(request)
                
                _uiState.update { it.copy(isSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro ao criar conta: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
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

            val outputFile = File.createTempFile("profile_", ".jpg", context.cacheDir)

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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
