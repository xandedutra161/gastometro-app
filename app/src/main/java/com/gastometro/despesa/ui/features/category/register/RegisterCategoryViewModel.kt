package com.gastometro.despesa.ui.features.category.register



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastometro.despesa.data.model.Category
import com.gastometro.despesa.repositories.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RegisterCategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
): ViewModel() {

    suspend fun insert(category: Category) = viewModelScope.launch {
        repository.insert(category)
    }

    suspend fun update(category: Category) = viewModelScope.launch {
        repository.update(category)
    }

    suspend fun isCategoryExists(name: String): Boolean {
        return repository.isCategoryExists(name)
    }

}