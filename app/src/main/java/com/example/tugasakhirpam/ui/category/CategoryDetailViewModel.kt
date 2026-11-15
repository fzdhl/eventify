package com.example.tugasakhirpam.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.CategoryRepository
import com.example.tugasakhirpam.model.Category
import com.example.tugasakhirpam.model.CategoryInUseException
import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryDetailUiState(
    val isLoading: Boolean = true,
    val category: Category? = null,
    val events: List<Event> = emptyList(),
    val errorMessage: String? = null,
    val isProcessing: Boolean = false,
    val deletedMessage: String? = null,
)

class CategoryDetailViewModel(
    private val repository: CategoryRepository = AppContainer.categoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryDetailUiState())
    val uiState: StateFlow<CategoryDetailUiState> = _uiState.asStateFlow()

    fun load(categoryId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.category == null, errorMessage = null) }
            runSafely { repository.getCategory(categoryId) to repository.getEventsByCategory(categoryId) }
                .onSuccess { (category, events) ->
                    _uiState.update { it.copy(isLoading = false, category = category, events = events) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.toUserMessage()) }
                }
        }
    }

    fun delete() {
        val category = _uiState.value.category ?: return
        if (_uiState.value.isProcessing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            runSafely { repository.deleteCategory(category.id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(isProcessing = false, deletedMessage = "Kategori \"${category.name}\" berhasil dihapus")
                    }
                }
                .onFailure { e ->
                    val message = if (e is CategoryInUseException) e.message else e.toUserMessage()
                    _uiState.update { it.copy(isProcessing = false, errorMessage = message) }
                }
        }
    }

    fun errorShown() = _uiState.update { it.copy(errorMessage = null) }
}
