package com.example.tugasakhirpam.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.AuthRepository
import com.example.tugasakhirpam.data.AuthState
import com.example.tugasakhirpam.data.AvatarUpload
import com.example.tugasakhirpam.data.ProfileInput
import com.example.tugasakhirpam.data.ProfileRepository
import com.example.tugasakhirpam.util.Validators
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val name: String = "",
    val nim: String = "",
    val email: String = "",
    val phone: String = "",
    val studyProgram: String = "",
    val currentAvatarUrl: String? = null,
    /** URI foto baru yang dipilih (untuk pratinjau), belum diunggah. */
    val pickedAvatarUri: String? = null,
    val nameError: String? = null,
    val nimError: String? = null,
    val phoneError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class EditProfileViewModel(
    private val repository: ProfileRepository = AppContainer.profileRepository,
    auth: AuthRepository = AppContainer.authRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        (auth.state.value as? AuthState.SignedIn)?.user.let { user ->
            EditProfileUiState(
                name = user?.name.orEmpty(),
                nim = user?.nim.orEmpty(),
                email = user?.email.orEmpty(),
                phone = user?.phone.orEmpty(),
                studyProgram = user?.studyProgram.orEmpty(),
                currentAvatarUrl = user?.avatarUrl,
            )
        }
    )
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var pendingAvatar: AvatarUpload? = null

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v.take(60), nameError = null) }
    fun onNimChange(v: String) = _uiState.update { it.copy(nim = v.filter(Char::isDigit).take(20), nimError = null) }
    fun onPhoneChange(v: String) = _uiState.update { it.copy(phone = v.take(20), phoneError = null) }
    fun onStudyProgramChange(v: String) = _uiState.update { it.copy(studyProgram = v.take(60)) }
    fun errorShown() = _uiState.update { it.copy(errorMessage = null) }

    /** Dipanggil layar setelah user memilih foto dan byte-nya selesai dibaca. */
    fun onAvatarPicked(uri: String, bytes: ByteArray, mimeType: String?) {
        when {
            mimeType !in ALLOWED_MIME ->
                _uiState.update { it.copy(errorMessage = "Gunakan foto berformat JPG atau PNG.") }

            bytes.size > MAX_AVATAR_BYTES ->
                _uiState.update { it.copy(errorMessage = "Ukuran foto maksimal 5 MB.") }

            else -> {
                pendingAvatar = AvatarUpload(bytes, mimeType!!)
                _uiState.update { it.copy(pickedAvatarUri = uri) }
            }
        }
    }

    fun onAvatarReadFailed() =
        _uiState.update { it.copy(errorMessage = "Foto tidak dapat dibaca. Coba pilih foto lain.") }

    fun submit() {
        val s = _uiState.value
        if (s.isSubmitting) return

        val nameError = Validators.fullName(s.name)
        val nimError = Validators.nim(s.nim)
        val phoneError = Validators.phoneOptional(s.phone)
        _uiState.update { it.copy(nameError = nameError, nimError = nimError, phoneError = phoneError) }
        if (nameError != null || nimError != null || phoneError != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val input = ProfileInput(s.name, s.nim, s.phone, s.studyProgram)
            runSafely { repository.updateProfile(input, pendingAvatar) }
                .onSuccess {
                    pendingAvatar = null
                    _uiState.update { it.copy(isSubmitting = false, successMessage = "Profil berhasil diperbarui") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = e.toUserMessage()) }
                }
        }
    }

    private companion object {
        val ALLOWED_MIME = setOf("image/jpeg", "image/png")
        const val MAX_AVATAR_BYTES = 5 * 1024 * 1024
    }
}
