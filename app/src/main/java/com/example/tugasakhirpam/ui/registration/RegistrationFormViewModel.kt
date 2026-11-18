package com.example.tugasakhirpam.ui.registration

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.EventRepository
import com.example.tugasakhirpam.data.RegistrationRepository
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.RegistrationInput
import com.example.tugasakhirpam.model.RegistrationUpdate
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventOption(val event: Event, val disabledReason: String?)

data class RegistrationFormUiState(
    val isEditMode: Boolean = false,
    val isLoadingInitial: Boolean = true,
    val options: List<EventOption> = emptyList(),
    val selectedEvent: Event? = null,
    val fullName: String = "",
    val nim: String = "",
    val email: String = "",
    val phone: String = "",
    val eventError: String? = null,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val hasErrors: Boolean
        get() = listOf(eventError, fullNameError, emailError, phoneError).any { it != null }
}

class RegistrationFormViewModel(
    private val registrationRepository: RegistrationRepository = AppContainer.registrationRepository,
    private val eventRepository: EventRepository = AppContainer.eventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationFormUiState())
    val uiState: StateFlow<RegistrationFormUiState> = _uiState.asStateFlow()

    private var initialized = false
    private var editingId: Long? = null

    fun init(eventId: Long?, registrationId: Long?) {
        if (initialized) return
        initialized = true
        editingId = registrationId

        viewModelScope.launch {
            _uiState.update { it.copy(isEditMode = registrationId != null, isLoadingInitial = true) }

            if (registrationId != null) {
                runSafely { registrationRepository.getRegistration(registrationId) }
                    .onSuccess { reg ->
                        _uiState.update {
                            it.copy(
                                isLoadingInitial = false,
                                selectedEvent = reg.event,
                                fullName = reg.fullName,
                                nim = reg.nim,
                                email = reg.email,
                                phone = reg.phone,
                            )
                        }
                    }
                    .onFailure { e -> _uiState.update { it.copy(isLoadingInitial = false, errorMessage = e.toUserMessage()) } }
                return@launch
            }

            val user = AppContainer.currentUser
            runSafely {
                val events = eventRepository.getEvents().filter { !it.isPast }
                val registeredIds = registrationRepository.getMyRegistrations()
                    .filter { it.isActive }
                    .map { it.eventId }
                    .toSet()
                events.map { event ->
                    val reason = when {
                        event.createdBy == user.id -> "Event milikmu"
                        event.id in registeredIds -> "Sudah terdaftar"
                        event.isFull -> "Kuota penuh"
                        else -> null
                    }
                    EventOption(event, reason)
                }
            }.onSuccess { options ->
                val preselected = options.firstOrNull { it.event.id == eventId }
                _uiState.update {
                    it.copy(
                        isLoadingInitial = false,
                        options = options,
                        selectedEvent = preselected?.takeIf { o -> o.disabledReason == null }?.event,
                        errorMessage = preselected?.disabledReason,
                        fullName = user.name,
                        nim = user.nim,
                        email = user.email,
                        phone = user.phone,
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoadingInitial = false, errorMessage = e.toUserMessage()) }
            }
        }
    }

    fun onEventSelected(option: EventOption) {
        if (option.disabledReason != null) {
            _uiState.update { it.copy(errorMessage = "${option.event.title}: ${option.disabledReason}") }
            return
        }
        _uiState.update { it.copy(selectedEvent = option.event, eventError = null) }
    }

    fun onFullNameChange(v: String) = _uiState.update { it.copy(fullName = v.take(60), fullNameError = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v.trim(), emailError = null) }
    fun onPhoneChange(v: String) = _uiState.update {
        it.copy(phone = v.filter { c -> c.isDigit() || c == '+' || c == ' ' || c == '-' }.take(20), phoneError = null)
    }
    fun errorShown() = _uiState.update { it.copy(errorMessage = null) }

    fun submit() {
        val validated = validate(_uiState.value)
        _uiState.value = validated
        if (validated.hasErrors || validated.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val s = _uiState.value
            val phone = s.phone.replace(Regex("[\\s-]"), "")
            val id = editingId

            runSafely {
                if (id != null) {
                    registrationRepository.updateRegistration(
                        id, RegistrationUpdate(fullName = s.fullName.trim(), email = s.email, phone = phone)
                    )
                } else {
                    registrationRepository.createRegistration(
                        RegistrationInput(
                            eventId = s.selectedEvent!!.id,
                            fullName = s.fullName.trim(),
                            nim = s.nim,
                            email = s.email,
                            phone = phone,
                        )
                    )
                }
            }.onSuccess {
                val msg = if (id != null) "Data pendaftaran diperbarui"
                else "Berhasil mendaftar ke ${s.selectedEvent?.title}"
                _uiState.update { it.copy(isSubmitting = false, successMessage = msg) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSubmitting = false, errorMessage = e.toUserMessage()) }
            }
        }
    }

    private fun validate(s: RegistrationFormUiState): RegistrationFormUiState = s.copy(
        eventError = if (s.selectedEvent == null) "Pilih event terlebih dahulu" else null,
        fullNameError = when {
            s.fullName.isBlank() -> "Nama lengkap wajib diisi"
            s.fullName.trim().length < 3 -> "Nama minimal 3 karakter"
            else -> null
        },
        emailError = when {
            s.email.isBlank() -> "Email wajib diisi"
            !Patterns.EMAIL_ADDRESS.matcher(s.email).matches() -> "Format email tidak valid"
            else -> null
        },
        phoneError = when {
            s.phone.isBlank() -> "Nomor HP wajib diisi"
            !PHONE_REGEX.matches(s.phone.trim()) -> "Nomor HP tidak valid, contoh: +62 812 3456 7890"
            else -> null
        },
    )

    private companion object {
        val PHONE_REGEX = Regex("^(\\+62|62|0)8[0-9\\s-]{7,15}$")
    }
}
