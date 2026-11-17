package com.example.tugasakhirpam.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.AuthRepository
import com.example.tugasakhirpam.data.LoginPrefs
import com.example.tugasakhirpam.util.Validators
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toAuthMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val rememberMe: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isSubmitting: Boolean = false,
    /** Pesan sekali tampil (snackbar), berisi error maupun info. */
    val message: String? = null,
)

class LoginViewModel(
    private val auth: AuthRepository = AppContainer.authRepository,
    private val prefs: LoginPrefs = AppContainer.loginPrefs,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        prefs.rememberedEmail().let { LoginUiState(email = it.orEmpty(), rememberMe = it != null) }
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailError = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, passwordError = null) }
    fun togglePasswordVisible() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun onRememberChange(v: Boolean) = _uiState.update { it.copy(rememberMe = v) }
    fun messageShown() = _uiState.update { it.copy(message = null) }

    fun submit() {
        val s = _uiState.value
        if (s.isSubmitting) return

        val emailError = Validators.email(s.email)
        val passwordError = if (s.password.isEmpty()) "Password wajib diisi" else null
        _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
        if (emailError != null || passwordError != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, message = null) }
            runSafely { auth.signIn(s.email, s.password) }
                .onSuccess {
                    prefs.save(s.rememberMe, s.email.trim())
                    // isSubmitting sengaja tetap true: AuthGate akan mengganti layar
                    // begitu profil selesai dimuat.
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, message = e.toAuthMessage()) }
                }
        }
    }

    fun forgotPassword() {
        val s = _uiState.value
        if (s.isSubmitting) return
        val emailError = Validators.email(s.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError, message = "Isi email dulu, lalu ketuk Lupa password.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, message = null) }
            runSafely { auth.sendPasswordReset(s.email) }
                .onSuccess {
                    _uiState.update {
                        it.copy(isSubmitting = false, message = "Tautan reset password dikirim ke ${s.email.trim()}.")
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, message = e.toAuthMessage()) }
                }
        }
    }
}
