package com.gastometro.despesa.ui.features.graphic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.gastometro.despesa.data.model.CategorySpending
import com.gastometro.despesa.repositories.MovementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GraphicViewModel @Inject constructor(
    private val repository: MovementRepository
) : ViewModel() {
    private val _btnSelected = MutableLiveData<String>()

    // Use Transformations.switchMap para observar _btnSelected e atualizar categorySpendings
    val categorySpendings: LiveData<List<CategorySpending>> = _btnSelected.switchMap { query ->
        fetchCategorySpendings(query)
    }

    // Use Transformations.switchMap para observar _btnSelected e atualizar categorySpendings
    val amountTotalMonth: LiveData<Double> = _btnSelected.switchMap { query ->
        fechAmountTotalMonth(query)
    }

    fun setMonthAndYear(query: String) {
        _btnSelected.value = query
    }

    // Replace this with your actual data fetching logic
    private fun fetchCategorySpendings(query: String): LiveData<List<CategorySpending>> {
        return repository.getTopSpendingCategories(query)
    }

    private fun fechAmountTotalMonth(query: String): LiveData<Double> {
        return repository.getAmountSpentOfMonth(query)
    }

}

