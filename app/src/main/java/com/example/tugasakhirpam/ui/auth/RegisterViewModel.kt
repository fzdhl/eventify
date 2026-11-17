package com.example.tugasakhirpam.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.AuthRepository
import com.example.tugasakhirpam.data.RegisterInput
import com.example.tugasakhirpam.data.SignUpResult
import com.example.tugasakhirpam.util.Validators
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toAuthMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val nim: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmVisible: Boolean = false,
    val acceptTerms: Boolean = false,
    val nameError: String? = null,
    val nimError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val termsError: String? = null,
    val isSubmitting: Boolean = false,
    val message: String? = null,
    /** Terisi saat pendaftaran sukses tetapi user masih harus memverifikasi email. */
    val confirmationMessage: String? = null,
)

class RegisterViewModel(
    private val auth: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v.take(60), nameError = null) }
    fun onNimChange(v: String) = _uiState.update { it.copy(nim = v.filter(Char::isDigit).take(20), nimError = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, emailError = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, passwordError = null, confirmError = null) }
    fun onConfirmChange(v: String) = _uiState.update { it.copy(confirmPassword = v, confirmError = null) }
    fun togglePasswordVisible() = _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun toggleConfirmVisible() = _uiState.update { it.copy(confirmVisible = !it.confirmVisible) }
    fun onTermsChange(v: Boolean) = _uiState.update { it.copy(acceptTerms = v, termsError = null) }
    fun messageShown() = _uiState.update { it.copy(message = null) }
    fun confirmationHandled() = _uiState.update { it.copy(confirmationMessage = null) }

    fun submit() {
        val s = _uiState.value
        if (s.isSubmitting) return

        val nameError = Validators.fullName(s.name)
        val nimError = Validators.nim(s.nim)
        val emailError = Validators.email(s.email)
        val passwordError = Validators.password(s.password)
        val confirmError = Validators.confirmPassword(s.password, s.confirmPassword)
        val termsError = if (s.acceptTerms) null else "Setujui Syarat & Ketentuan untuk melanjutkan"

        _uiState.update {
            it.copy(
                nameError = nameError,
                nimError = nimError,
                emailError = emailError,
                passwordError = passwordError,
                confirmError = confirmError,
                termsError = termsError,
            )
        }
        if (listOf(nameError, nimError, emailError, passwordError, confirmError, termsError).any { it != null }) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, message = null) }
            val input = RegisterInput(name = s.name, nim = s.nim, email = s.email, password = s.password)
            runSafely { auth.signUp(input) }
                .onSuccess { result ->
                    when (result) {
                        // isSubmitting tetap true: AuthGate akan mengganti layar saat profil dimuat.
                        SignUpResult.SIGNED_IN -> Unit
                        SignUpResult.EMAIL_CONFIRMATION_REQUIRED -> _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                confirmationMessage = "Akun dibuat. Buka email ${input.email.trim()} untuk verifikasi, lalu masuk.",
                            )
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, message = e.toAuthMessage()) }
                }
        }
    }
}
