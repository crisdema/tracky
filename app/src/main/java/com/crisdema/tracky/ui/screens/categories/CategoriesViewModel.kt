package com.crisdema.tracky.ui.screens.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crisdema.tracky.data.model.Category
import com.crisdema.tracky.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoriesUiState(
    val categories: List<Category> = emptyList()
)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val spaceId: String = savedStateHandle.get<String>("spaceId")
        ?: savedStateHandle.get<String>("id")
        ?: ""

    init {
        if (spaceId.isNotBlank()) {
            categoryRepository.startRemoteSync(spaceId)
        }
    }

    val uiState: StateFlow<CategoriesUiState> = categoryRepository
        .observeCategories(spaceId)
        .map { categories -> CategoriesUiState(categories = categories) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoriesUiState()
        )

    fun addCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.addCategory(category.copy(spaceId = spaceId))
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            categoryRepository.deleteCategory(spaceId, categoryId)
        }
    }

    fun reorderExpenseCategories(reordered: List<Category>) {
        viewModelScope.launch {
            categoryRepository.reorderCategories(spaceId, reordered)
        }
    }

    fun reorderIncomeCategories(reordered: List<Category>) {
        viewModelScope.launch {
            categoryRepository.reorderCategories(spaceId, reordered)
        }
    }
}