package com.example.tugasakhirpam.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.RegistrationRepository
import com.example.tugasakhirpam.model.Registration
import com.example.tugasakhirpam.model.RegistrationStatus
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RegistrationTab(val label: String) {
    ALL("All"),
    UPCOMING("Upcoming"),
    CANCELLED("Cancelled"),
}

private fun List<Registration>.filterBy(tab: RegistrationTab): List<Registration> = when (tab) {
    RegistrationTab.ALL -> this
    RegistrationTab.UPCOMING -> filter { it.isActive && it.event?.isPast == false }
    RegistrationTab.CANCELLED -> filter { it.status == RegistrationStatus.CANCELLED }
}

data class RegistrationListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val registrations: List<Registration> = emptyList(),
    val selectedTab: RegistrationTab = RegistrationTab.ALL,
    val errorMessage: String? = null,
) {
    val filtered: List<Registration> get() = registrations.filterBy(selectedTab)
    fun countFor(tab: RegistrationTab): Int = registrations.filterBy(tab).size
}

class RegistrationListViewModel(
    private val repository: RegistrationRepository = AppContainer.registrationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationListUiState())
    val uiState: StateFlow<RegistrationListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadRegistrations() = fetch(isRefresh = false)
    fun refresh() = fetch(isRefresh = true)
    fun selectTab(tab: RegistrationTab) = _uiState.update { it.copy(selectedTab = tab) }
    fun errorShown() = _uiState.update { it.copy(errorMessage = null) }

    private fun fetch(isRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh && it.registrations.isEmpty(),
                    isRefreshing = isRefresh,
                    errorMessage = null,
                )
            }
            runSafely { repository.getMyRegistrations() }
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, registrations = list) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, errorMessage = e.toUserMessage()) }
                }
        }
    }
}
