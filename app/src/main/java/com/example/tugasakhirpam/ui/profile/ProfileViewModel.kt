package com.example.tugasakhirpam.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.AuthRepository
import com.example.tugasakhirpam.data.AuthState
import com.example.tugasakhirpam.model.User
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val isLoggingOut: Boolean = false,
    val errorMessage: String? = null,
)

class ProfileViewModel(
    private val auth: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(user = (auth.state.value as? AuthState.SignedIn)?.user)
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Ikut berubah otomatis saat Edit Profil menyimpan data baru.
        viewModelScope.launch {
            auth.state.collect { state ->
                if (state is AuthState.SignedIn) _uiState.update { it.copy(user = state.user) }
            }
        }
    }

    fun logout() {
        if (_uiState.value.isLoggingOut) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, errorMessage = null) }
            // Sukses: AuthGate otomatis kembali ke layar login.
            runSafely { auth.signOut() }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoggingOut = false, errorMessage = e.toUserMessage()) }
                }
        }
    }

    fun errorShown() = _uiState.update { it.copy(errorMessage = null) }
}
