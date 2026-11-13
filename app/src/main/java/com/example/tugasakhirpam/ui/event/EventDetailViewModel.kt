package com.example.tugasakhirpam.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.EventRepository
import com.example.tugasakhirpam.data.RegistrationRepository
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.Registration
import com.example.tugasakhirpam.model.RegistrationStatus
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RegisterAction { REGISTER, VIEW_REGISTRATION, EVENT_PAST, QUOTA_FULL }

data class EventDetailUiState(
    val isLoading: Boolean = true,
    val event: Event? = null,
    val isOwner: Boolean = false,
    val myRegistration: Registration? = null,
    val participants: List<Registration> = emptyList(),
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val deletedMessage: String? = null,
) {
    val registerAction: RegisterAction
        get() = when {
            myRegistration != null -> RegisterAction.VIEW_REGISTRATION
            event?.isPast == true -> RegisterAction.EVENT_PAST
            event?.isFull == true -> RegisterAction.QUOTA_FULL
            else -> RegisterAction.REGISTER
        }
}

class EventDetailViewModel(
    private val eventRepository: EventRepository = AppContainer.eventRepository,
    private val registrationRepository: RegistrationRepository = AppContainer.registrationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private val currentUserId = AppContainer.currentUser.id

    fun load(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.event == null, errorMessage = null) }
            runSafely {
                val event = eventRepository.getEvent(eventId)
                val isOwner = event.createdBy == currentUserId
                EventDetailUiState(
                    isLoading = false,
                    event = event,
                    isOwner = isOwner,
                    myRegistration = if (isOwner) null else registrationRepository.findMyRegistration(eventId),
                    participants = if (isOwner) eventRepository.getParticipants(eventId) else emptyList(),
                )
            }.onSuccess { loaded ->
                _uiState.update { loaded.copy(snackbarMessage = it.snackbarMessage) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) }
            }
        }
    }

    fun toggleFavorite() {
        val event = _uiState.value.event ?: return
        val newValue = !event.isFavorite
        _uiState.update { it.copy(event = event.copy(isFavorite = newValue)) }

        viewModelScope.launch {
            runSafely { eventRepository.toggleFavorite(event.id) }
                .onSuccess { saved ->
                    val msg = if (saved) "Ditambahkan ke Favorites" else "Dihapus dari Favorites"
                    _uiState.update { it.copy(snackbarMessage = msg) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(event = event, snackbarMessage = e.toUserMessage()) }
                }
        }
    }

    fun updateParticipantStatus(registrationId: Long, status: RegistrationStatus) {
        val event = _uiState.value.event ?: return
        if (_uiState.value.isProcessing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            runSafely {
                registrationRepository.updateStatus(registrationId, status)
                eventRepository.getEvent(event.id) to eventRepository.getParticipants(event.id)
            }.onSuccess { (updatedEvent, participants) ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        event = updatedEvent,
                        participants = participants,
                        snackbarMessage = "Status peserta diubah menjadi ${status.label}",
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isProcessing = false, snackbarMessage = e.toUserMessage()) }
            }
        }
    }

    fun deleteEvent() {
        val event = _uiState.value.event ?: return
        if (_uiState.value.isProcessing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            runSafely { eventRepository.deleteEvent(event.id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(isProcessing = false, deletedMessage = "Event \"${event.title}\" berhasil dihapus")
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isProcessing = false, snackbarMessage = e.toUserMessage()) }
                }
        }
    }

    fun snackbarShown() = _uiState.update { it.copy(snackbarMessage = null) }
}
