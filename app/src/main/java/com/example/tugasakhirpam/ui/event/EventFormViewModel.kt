package com.example.tugasakhirpam.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.EventRepository
import com.example.tugasakhirpam.model.EventCategory
import com.example.tugasakhirpam.model.EventInput
import com.example.tugasakhirpam.util.DateFormatter
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class EventFormUiState(
    val isEditMode: Boolean = false,
    val isLoadingInitial: Boolean = false,
    val posterUri: String? = null,
    val title: String = "",
    val category: EventCategory? = null,
    val description: String = "",
    val eventDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val quota: String = "",
    val registeredCount: Int = 0,
    val titleError: String? = null,
    val categoryError: String? = null,
    val descriptionError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null,
    val locationError: String? = null,
    val quotaError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val hasErrors: Boolean
        get() = listOf(titleError, categoryError, descriptionError, dateError, timeError, locationError, quotaError)
            .any { it != null }
}

class EventFormViewModel(
    private val repository: EventRepository = AppContainer.eventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventFormUiState())
    val uiState: StateFlow<EventFormUiState> = _uiState.asStateFlow()

    private var initialized = false
    private var editingId: Long? = null

    fun init(eventId: Long?) {
        if (initialized) return
        initialized = true
        editingId = eventId
        if (eventId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isEditMode = true, isLoadingInitial = true) }
            runSafely { repository.getEvent(eventId) }
                .onSuccess { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingInitial = false,
                            posterUri = e.imageUrl,
                            title = e.title,
                            category = e.category,
                            description = e.description,
                            eventDate = e.eventDate,
                            startTime = e.startTime,
                            endTime = e.endTime,
                            location = e.location,
                            quota = e.quota.toString(),
                            registeredCount = e.registeredCount,
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoadingInitial = false, errorMessage = e.toUserMessage()) } }
        }
    }

    fun onPosterSelected(uri: String) = _uiState.update { it.copy(posterUri = uri) }
    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v.take(80), titleError = null) }
    fun onCategorySelected(c: EventCategory) = _uiState.update { it.copy(category = c, categoryError = null) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v.take(1000), descriptionError = null) }
    fun onDateSelected(date: String) = _uiState.update { it.copy(eventDate = date, dateError = null, timeError = null) }
    fun onStartTimeSelected(time: String) = _uiState.update { it.copy(startTime = time, timeError = null) }
    fun onEndTimeSelected(time: String) = _uiState.update { it.copy(endTime = time, timeError = null) }
    fun onLocationChange(v: String) = _uiState.update { it.copy(location = v.take(100), locationError = null) }
    fun onQuotaChange(v: String) = _uiState.update { it.copy(quota = v.filter(Char::isDigit).take(5), quotaError = null) }
    fun errorShown() = _uiState.update { it.copy(errorMessage = null) }

    fun submit() {
        val validated = validate(_uiState.value)
        _uiState.value = validated
        if (validated.hasErrors || validated.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val s = _uiState.value
            val input = EventInput(
                title = s.title.trim(),
                category = s.category!!,
                description = s.description.trim(),
                eventDate = s.eventDate,
                startTime = s.startTime,
                endTime = s.endTime,
                location = s.location.trim(),
                quota = s.quota.toInt(),
                imageUrl = s.posterUri,
            )
            val id = editingId
            runSafely {
                if (id == null) repository.createEvent(input) else repository.updateEvent(id, input)
            }.onSuccess {
                val msg = if (id == null) "Event \"${input.title}\" berhasil dibuat" else "Perubahan event disimpan"
                _uiState.update { it.copy(isSubmitting = false, successMessage = msg) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSubmitting = false, errorMessage = e.toUserMessage()) }
            }
        }
    }

    private fun validate(s: EventFormUiState): EventFormUiState {
        val date = DateFormatter.parseDate(s.eventDate)
        val start = DateFormatter.parseTime(s.startTime)
        val end = DateFormatter.parseTime(s.endTime)
        val quota = s.quota.toIntOrNull()

        return s.copy(
            titleError = when {
                s.title.isBlank() -> "Nama event wajib diisi"
                s.title.trim().length < 5 -> "Nama event minimal 5 karakter"
                else -> null
            },
            categoryError = if (s.category == null) "Pilih kategori event" else null,
            descriptionError = when {
                s.description.isBlank() -> "Deskripsi wajib diisi"
                s.description.trim().length < 20 -> "Deskripsi minimal 20 karakter agar peserta paham isi event"
                else -> null
            },
            dateError = when {
                date == null -> "Pilih tanggal event"
                date.isBefore(LocalDate.now()) -> "Tanggal tidak boleh di masa lalu"
                else -> null
            },
            timeError = if (start == null || end == null) {
                "Pilih waktu mulai dan selesai"
            } else when {
                !end.isAfter(start) -> "Waktu selesai harus setelah waktu mulai"
                date == LocalDate.now() && !start.isAfter(LocalTime.now()) -> "Waktu mulai hari ini sudah lewat"
                else -> null
            },
            locationError = when {
                s.location.isBlank() -> "Lokasi wajib diisi"
                s.location.trim().length < 3 -> "Lokasi terlalu pendek"
                else -> null
            },
            quotaError = when {
                quota == null -> "Kuota wajib diisi"
                quota < 1 -> "Kuota minimal 1 peserta"
                quota > 10_000 -> "Kuota maksimal 10.000 peserta"
                s.isEditMode && quota < s.registeredCount ->
                    "Kuota tidak boleh kurang dari ${s.registeredCount} peserta terdaftar"
                else -> null
            },
        )
    }
}
