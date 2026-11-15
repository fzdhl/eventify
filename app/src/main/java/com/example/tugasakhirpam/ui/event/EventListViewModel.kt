package com.example.tugasakhirpam.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.CategoryRepository
import com.example.tugasakhirpam.data.EventRepository
import com.example.tugasakhirpam.model.Category
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EventListSource { ALL, FAVORITES, MY_EVENTS }

data class EventListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val events: List<Event> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
) {
    val filtered: List<Event>
        get() = if (selectedCategoryId == null) events else events.filter { it.categoryId == selectedCategoryId }
}

class EventListViewModel(
    private val repository: EventRepository = AppContainer.eventRepository,
    private val categoryRepository: CategoryRepository = AppContainer.categoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventListUiState())
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()

    private var source = EventListSource.ALL
    private var initialized = false
    private var loadJob: Job? = null

    fun start(source: EventListSource, initialCategoryId: Long? = null) {
        if (!initialized) {
            initialized = true
            this.source = source
            _uiState.update { it.copy(selectedCategoryId = initialCategoryId) }
        }
        fetch(isRefresh = false)
    }

    fun refresh() = fetch(isRefresh = true)

    fun selectCategory(categoryId: Long?) = _uiState.update { it.copy(selectedCategoryId = categoryId) }

    fun snackbarShown() = _uiState.update { it.copy(snackbarMessage = null) }

    fun toggleFavorite(eventId: Long) {
        val before = _uiState.value.events
        val target = before.firstOrNull { it.id == eventId } ?: return
        val newValue = !target.isFavorite

        _uiState.update { state ->
            val updated = if (source == EventListSource.FAVORITES && !newValue) {
                state.events.filterNot { it.id == eventId }
            } else {
                state.events.map { if (it.id == eventId) it.copy(isFavorite = newValue) else it }
            }
            state.copy(events = updated)
        }

        viewModelScope.launch {
            runSafely { repository.toggleFavorite(eventId) }
                .onSuccess { saved ->
                    val msg = if (saved) "Ditambahkan ke Favorites" else "Dihapus dari Favorites"
                    _uiState.update { it.copy(snackbarMessage = msg) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(events = before, snackbarMessage = e.toUserMessage()) }
                }
        }
    }

    private fun fetch(isRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh && it.events.isEmpty(),
                    isRefreshing = isRefresh,
                    errorMessage = null,
                )
            }
            runSafely {
                val events = when (source) {
                    EventListSource.ALL -> repository.getEvents().filter { !it.isPast }
                    EventListSource.FAVORITES -> repository.getFavoriteEvents()
                    EventListSource.MY_EVENTS -> repository.getMyEvents()
                }
                events to categoryRepository.getCategories()
            }.onSuccess { (events, categories) ->
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, events = events, categories = categories)
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = e.toUserMessage()) }
            }
        }
    }
}
