package com.gastometro.despesa.ui.features.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastometro.despesa.data.model.Category
import com.gastometro.despesa.repositories.CategoryRepository
import com.gastometro.despesa.ui.state.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListCategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _categories =
        MutableStateFlow<ResourceState<List<Category>>>(ResourceState.Empty())
    val categories: StateFlow<ResourceState<List<Category>>> = _categories

    init {
        fetch()
    }

    private fun fetch() = viewModelScope.launch {
        repository.getAll().collectLatest {
            if (it.isNullOrEmpty()) {
                _categories.value = ResourceState.Empty()
            } else {
                _categories.value = ResourceState.Success(it)
            }
        }
    }

    fun delete(category: Category) = viewModelScope.launch {
        repository.delete(category)
    }

}