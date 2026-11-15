package com.example.tugasakhirpam.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.EventRepository
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val events: List<Event> = emptyList(),
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
)

class FavoritesViewModel(
    private val repository: EventRepository = AppContainer.eventRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadFavorites() = fetch(isRefresh = false)
    fun refresh() = fetch(isRefresh = true)

    fun snackbarShown() = _uiState.update { it.copy(snackbarMessage = null) }

    fun toggleFavorite(eventId: Long) {
        val before = _uiState.value.events
        _uiState.update { state ->
            state.copy(events = state.events.filterNot { it.id == eventId })
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
            runSafely { repository.getFavoriteEvents() }
                .onSuccess { events ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, events = events) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = e.toUserMessage()) }
                }
        }
    }
}
