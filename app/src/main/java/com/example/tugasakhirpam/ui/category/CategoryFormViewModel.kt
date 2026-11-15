package com.example.tugasakhirpam.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tugasakhirpam.data.AppContainer
import com.example.tugasakhirpam.data.CategoryRepository
import com.example.tugasakhirpam.model.CategoryInput
import com.example.tugasakhirpam.model.DuplicateCategoryNameException
import com.example.tugasakhirpam.util.runSafely
import com.example.tugasakhirpam.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryFormUiState(
    val isEditMode: Boolean = false,
    val isLoadingInitial: Boolean = false,
    val name: String = "",
    val description: String = "",
    val nameError: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

class CategoryFormViewModel(
    private val repository: CategoryRepository = AppContainer.categoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryFormUiState())
    val uiState: StateFlow<CategoryFormUiState> = _uiState.asStateFlow()

    private var initialized = false
    private var editingId: Long? = null

    fun init(categoryId: Long?) {
        if (initialized) return
        initialized = true
        editingId = categoryId
        if (categoryId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isEditMode = true, isLoadingInitial = true) }
            runSafely { repository.getCategory(categoryId) }
                .onSuccess { c ->
                    _uiState.update { it.copy(isLoadingInitial = false, name = c.name, description = c.description) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoadingInitial = false, errorMessage = e.toUserMessage()) }
                }
        }
    }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v.take(30), nameError = null) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v.take(100)) }
    fun errorShown() = _uiState.update { it.copy(errorMessage = null) }

    fun submit() {
        val current = _uiState.value
        val nameError = validateName(current.name)
        _uiState.update { it.copy(nameError = nameError) }
        if (nameError != null || current.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val input = CategoryInput(name = current.name.trim(), description = current.description.trim())
            val id = editingId
            runSafely {
                if (id == null) repository.createCategory(input) else repository.updateCategory(id, input)
            }.onSuccess {
                val msg = if (id == null) "Kategori \"${input.name}\" berhasil dibuat" else "Perubahan kategori disimpan"
                _uiState.update { it.copy(isSubmitting = false, successMessage = msg) }
            }.onFailure { e ->
                _uiState.update {
                    if (e is DuplicateCategoryNameException) {
                        it.copy(isSubmitting = false, nameError = "Nama kategori sudah digunakan")
                    } else {
                        it.copy(isSubmitting = false, errorMessage = e.toUserMessage())
                    }
                }
            }
        }
    }

    private fun validateName(raw: String): String? = when {
        raw.isBlank() -> "Nama kategori wajib diisi"
        raw.trim().length < 3 -> "Nama kategori minimal 3 karakter"
        else -> null
    }
}
