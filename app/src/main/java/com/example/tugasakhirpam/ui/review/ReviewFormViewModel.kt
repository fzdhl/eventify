package com.example.tugasakhirpam.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.EventRepository
import com.example.tugasakhirpam.data.ReviewRepository
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.ReviewInput
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_REVIEW_LENGTH = 500

data class ReviewFormUiState(
    val isEditMode: Boolean = false,
    val isLoadingInitial: Boolean = true,
    val event: Event? = null,
    val rating: Int = 0,
    val comment: String = "",
    val ratingError: String? = null,
    val commentError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val hasErrors: Boolean get() = ratingError != null || commentError != null
}

class ReviewFormViewModel(
    private val reviewRepository: ReviewRepository = AppContainer.reviewRepository,
    private val eventRepository: EventRepository = AppContainer.eventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewFormUiState())
    val uiState: StateFlow<ReviewFormUiState> = _uiState.asStateFlow()

    private var initialized = false
    private var eventId: Long = 0
    private var editingId: Long? = null

    fun init(eventId: Long, reviewId: Long?) {
        if (initialized) return
        initialized = true
        this.eventId = eventId
        editingId = reviewId

        viewModelScope.launch {
            _uiState.update { it.copy(isEditMode = reviewId != null, isLoadingInitial = true) }
            try {
                val event = eventRepository.getEvent(eventId)
                val review = if (reviewId != null) reviewRepository.getReview(reviewId) else null
                _uiState.update {
                    it.copy(
                        isLoadingInitial = false,
                        event = event,
                        rating = review?.rating ?: 0,
                        comment = review?.comment.orEmpty(),
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingInitial = false, errorMessage = e.toUserMessage()) }
            }
        }
    }

    fun onRatingChange(rating: Int) {
        _uiState.update { it.copy(rating = rating.coerceIn(1, 5), ratingError = null) }
    }

    fun onCommentChange(value: String) {
        _uiState.update { it.copy(comment = value.take(MAX_REVIEW_LENGTH), commentError = null) }
    }

    fun errorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun submit() {
        val validated = validate(_uiState.value)
        _uiState.value = validated
        if (validated.hasErrors || validated.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val s = _uiState.value
            val input = ReviewInput(rating = s.rating, comment = s.comment.trim())
            val id = editingId
            try {
                if (id == null) {
                    reviewRepository.createReview(eventId, input)
                } else {
                    reviewRepository.updateReview(id, input)
                }
                val msg = if (id == null) "Terima kasih! Ulasanmu sudah terkirim" else "Ulasan diperbarui"
                _uiState.update { it.copy(isSubmitting = false, successMessage = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = e.toUserMessage()) }
            }
        }
    }

    private fun validate(s: ReviewFormUiState): ReviewFormUiState = s.copy(
        ratingError = if (s.rating !in 1..5) "Pilih rating 1 sampai 5 bintang" else null,
        commentError = when {
            s.comment.isBlank() -> "Ceritakan pengalamanmu di event ini"
            s.comment.trim().length < 10 -> "Ulasan minimal 10 karakter"
            else -> null
        },
    )
}
