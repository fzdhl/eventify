package com.example.tugasakhirpam.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.EventRepository
import com.example.tugasakhirpam.data.RegistrationRepository
import com.example.tugasakhirpam.data.ReviewRepository
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.RegistrationStatus
import com.example.tugasakhirpam.model.Review
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ReviewAction { WRITE, EDIT, EVENT_NOT_FINISHED, NOT_ATTENDED, OWNER }

data class ReviewListUiState(
    val isLoading: Boolean = true,
    val event: Event? = null,
    val reviews: List<Review> = emptyList(),
    val selectedRating: Int? = null,
    val action: ReviewAction = ReviewAction.NOT_ATTENDED,
    val currentUserId: String = AppContainer.currentUser.id,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
) {
    val average: Double get() = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()

    val filtered: List<Review>
        get() = if (selectedRating == null) reviews else reviews.filter { it.rating == selectedRating }

    val myReview: Review? get() = reviews.firstOrNull { it.userId == currentUserId }

    fun countFor(rating: Int): Int = reviews.count { it.rating == rating }
}

class ReviewListViewModel(
    private val reviewRepository: ReviewRepository = AppContainer.reviewRepository,
    private val eventRepository: EventRepository = AppContainer.eventRepository,
    private val registrationRepository: RegistrationRepository = AppContainer.registrationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewListUiState())
    val uiState: StateFlow<ReviewListUiState> = _uiState.asStateFlow()

    fun load(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.event == null, errorMessage = null) }
            try {
                val event = eventRepository.getEvent(eventId)
                val reviews = reviewRepository.getReviews(eventId)
                val myRegistration = registrationRepository.findMyRegistration(eventId)
                val userId = _uiState.value.currentUserId

                val action = when {
                    event.createdBy == userId -> ReviewAction.OWNER
                    reviews.any { it.userId == userId } -> ReviewAction.EDIT
                    !event.isPast -> ReviewAction.EVENT_NOT_FINISHED
                    myRegistration?.status != RegistrationStatus.CONFIRMED -> ReviewAction.NOT_ATTENDED
                    else -> ReviewAction.WRITE
                }
                _uiState.update { it.copy(isLoading = false, event = event, reviews = reviews, action = action) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) }
            }
        }
    }

    fun selectRating(rating: Int?) {
        _uiState.update { it.copy(selectedRating = rating) }
    }

    fun deleteReview(reviewId: Long) {
        val eventId = _uiState.value.event?.id ?: return
        if (_uiState.value.isProcessing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            try {
                reviewRepository.deleteReview(reviewId)
                _uiState.update { it.copy(isProcessing = false, snackbarMessage = "Ulasan dihapus") }
                load(eventId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, snackbarMessage = e.toUserMessage()) }
            }
        }
    }

    fun snackbarShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
