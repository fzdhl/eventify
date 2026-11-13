package com.example.tugasakhirpam.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.RegistrationRepository
import com.example.tugasakhirpam.model.Registration
import com.example.tugasakhirpam.model.RegistrationStatus
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationDetailUiState(
    val isLoading: Boolean = true,
    val registration: Registration? = null,
    val errorMessage: String? = null,
    val isProcessing: Boolean = false,
    val snackbarMessage: String? = null,
    val deletedMessage: String? = null,
)

class RegistrationDetailViewModel(
    private val repository: RegistrationRepository = AppContainer.registrationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationDetailUiState())
    val uiState: StateFlow<RegistrationDetailUiState> = _uiState.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.registration == null, errorMessage = null) }
            runSafely { repository.getRegistration(id) }
                .onSuccess { reg -> _uiState.update { it.copy(isLoading = false, registration = reg) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) } }
        }
    }

    fun cancel() = changeStatus(RegistrationStatus.CANCELLED, "Pendaftaran berhasil dibatalkan")

    fun reRegister() = changeStatus(RegistrationStatus.CONFIRMED, "Berhasil daftar ulang")

    private fun changeStatus(status: RegistrationStatus, successMessage: String) {
        val current = _uiState.value.registration ?: return
        if (current.status == status || _uiState.value.isProcessing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            runSafely {
                repository.updateStatus(current.id, status)
                repository.getRegistration(current.id)
            }.onSuccess { updated ->
                _uiState.update { it.copy(isProcessing = false, registration = updated, snackbarMessage = successMessage) }
            }.onFailure { e ->
                _uiState.update { it.copy(isProcessing = false, snackbarMessage = e.toUserMessage()) }
            }
        }
    }

    fun delete() {
        val current = _uiState.value.registration ?: return
        if (_uiState.value.isProcessing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            runSafely { repository.deleteRegistration(current.id) }
                .onSuccess { _uiState.update { it.copy(isProcessing = false, deletedMessage = "Pendaftaran dihapus") } }
                .onFailure { e -> _uiState.update { it.copy(isProcessing = false, snackbarMessage = e.toUserMessage()) } }
        }
    }

    fun snackbarShown() = _uiState.update { it.copy(snackbarMessage = null) }
}
