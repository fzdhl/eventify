package com.example.tugasakhirpam.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.CategoryRepository
import com.example.tugasakhirpam.model.Category
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
)

class CategoryListViewModel(
    private val repository: CategoryRepository = AppContainer.categoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryListUiState())
    val uiState: StateFlow<CategoryListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load(isRefresh: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh && it.categories.isEmpty(),
                    isRefreshing = isRefresh,
                    errorMessage = null,
                )
            }
            runSafely { repository.getCategories() }
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, isRefreshing = false, categories = list) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, errorMessage = e.toUserMessage())
                    }
                }
        }
    }

    fun refresh() = load(isRefresh = true)
}
