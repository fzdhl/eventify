package com.example.tugasakhirpam.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.CategoryRepository
import com.example.tugasakhirpam.data.EventRepository
import com.example.tugasakhirpam.model.Category
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.User
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val user: User = AppContainer.currentUser,
    val events: List<Event> = emptyList(),
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
) {
    private val upcoming: List<Event> get() = events.filter { !it.isPast }

    val featuredEvent: Event?
        get() = upcoming.firstOrNull { it.isFeatured } ?: upcoming.firstOrNull()

    val recommended: List<Event>
        get() = upcoming.filter { it.id != featuredEvent?.id && it.createdBy != user.id }

    fun search(query: String): List<Event> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        return upcoming.filter {
            it.title.contains(q, ignoreCase = true) ||
                it.categoryName.contains(q, ignoreCase = true) ||
                it.location.contains(q, ignoreCase = true) ||
                it.organizer.contains(q, ignoreCase = true)
        }
    }
}

class HomeViewModel(
    private val eventRepository: EventRepository = AppContainer.eventRepository,
    private val categoryRepository: CategoryRepository = AppContainer.categoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadEvents() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.events.isEmpty(), errorMessage = null) }
            runSafely { eventRepository.getEvents() to categoryRepository.getCategories() }
                .onSuccess { (events, categories) ->
                    _uiState.update { it.copy(isLoading = false, events = events, categories = categories) }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) } }
        }
    }
}
