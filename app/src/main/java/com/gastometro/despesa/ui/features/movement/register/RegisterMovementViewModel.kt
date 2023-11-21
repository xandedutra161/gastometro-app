package com.gastometro.despesa.ui.features.movement.register

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastometro.despesa.data.model.Category
import com.gastometro.despesa.data.model.Movement
import com.gastometro.despesa.repositories.CategoryRepository
import com.gastometro.despesa.repositories.MovementRepository
import com.gastometro.despesa.ui.state.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class RegisterMovementViewModel @Inject constructor(
    private val repositoryCategory: CategoryRepository,
    private val repositoryMovement: MovementRepository
) : ViewModel() {
    private val _categories =
        MutableStateFlow<ResourceState<List<Category>>>(ResourceState.Empty())
    val categories: StateFlow<ResourceState<List<Category>>> = _categories

    private val _selectedCategory = MutableLiveData<String>()
    val selectedCategory: LiveData<String> = _selectedCategory

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    init {
        fetch()
    }

    private fun fetch() = viewModelScope.launch {
        repositoryCategory.getAll().collectLatest {
            if (it.isEmpty()) {
                _categories.value = ResourceState.Empty()
            } else {
                _categories.value = ResourceState.Success(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insert(movement: Movement) = viewModelScope.launch {
        if (movement.numberInstallments > 0) {
            if (movement.fixed) insertInstallmentsFixed(movement) else insertInstallments(movement)
        } else {
            repositoryMovement.insert(movement)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun insertInstallments(movement: Movement) = viewModelScope.launch {
        val listMovement = mutableListOf<Movement>()

        val valueTotal = movement.amount
        val parcels = movement.numberInstallments
        val rest = BigDecimal(parcels).subtract(BigDecimal(1))
        val valueAmountParcel = valueTotal.divide(BigDecimal(parcels), 2, BigDecimal.ROUND_HALF_EVEN)
        var valueAmount: BigDecimal

        for (x in 1..movement.numberInstallments) {
            val newMonth = movement.date.plusMonths(x.toLong() - 1)

            valueAmount = if(x == movement.numberInstallments) {
                valueTotal.subtract(valueAmountParcel.multiply(rest))
            } else {
                valueAmountParcel
            }

            val newMovement = Movement(
                amount = valueAmount,
                paid = movement.paid,
                description = movement.description,
                date = newMonth,
                categoryName = movement.categoryName,
                fixed = movement.fixed,
                installments = movement.installments,
                numberInstallments = movement.numberInstallments,
                currentInstallments = x
            )
            listMovement.add(newMovement)
        }
        repositoryMovement.insertAll(listMovement)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun insertInstallmentsFixed(movement: Movement) = viewModelScope.launch {
        val listMovement = mutableListOf<Movement>()

        for (x in 1..movement.numberInstallments) {
            val newMonth = movement.date.plusMonths(x.toLong() - 1)

            val newMovement = Movement(
                amount = movement.amount,
                paid = movement.paid,
                description = movement.description,
                date = newMonth,
                categoryName = movement.categoryName,
                fixed = movement.fixed,
                installments = movement.installments,
                numberInstallments = movement.numberInstallments,
                currentInstallments = x
            )
            listMovement.add(newMovement)
        }
        repositoryMovement.insertAll(listMovement)
    }

    fun update(movement: Movement) = viewModelScope.launch {
        repositoryMovement.update(movement)
    }


    fun searchCategoryName(name: String) = viewModelScope.launch {
        repositoryMovement.searchCategoryName(name).collectLatest {
            if (it.isEmpty()) {
                _categories.value = ResourceState.Empty()
            } else {
                _categories.value = ResourceState.Success(it)
            }
        }
    }

    fun getAllCategories() = viewModelScope.launch {
        repositoryCategory.getAll().collectLatest {
            if (it.isEmpty()) {
                _categories.value = ResourceState.Empty()
            } else {
                _categories.value = ResourceState.Success(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateAllParcels(startId: Int, endId: Int, movement: Movement) = viewModelScope.launch {
        val listMovement = mutableListOf<Movement>()
        var count = 0
        val initialMonth = movement.date.minusMonths(movement.currentInstallments.toLong())

        for (x in startId..endId) {
            count++
            val updateMonth = initialMonth.plusMonths(count.toLong())

            val updateMovement = Movement(
                movementId = x,
                amount = movement.amount,
                paid = movement.paid,
                description = movement.description,
                date = updateMonth,
                categoryName = movement.categoryName,
                fixed = movement.fixed,
                installments = movement.installments,
                numberInstallments = movement.numberInstallments,
                currentInstallments = count
            )
            listMovement.add(updateMovement)
        }
        repositoryMovement.updateMovements(listMovement)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateCurrentAndSubsequentParcels(startId: Int, endId: Int, movement: Movement) =
        viewModelScope.launch {
            val listMovement = mutableListOf<Movement>()
            var count = 0
            for (x in startId..endId) {
                count++
                val updateMonth = movement.date.plusMonths(count.toLong() - 1)

                val updateMovement = Movement(
                    movementId = x,
                    amount = movement.amount,
                    paid = movement.paid,
                    description = movement.description,
                    date = updateMonth,
                    categoryName = movement.categoryName,
                    fixed = movement.fixed,
                    installments = movement.installments,
                    numberInstallments = movement.numberInstallments,
                    currentInstallments = (movement.currentInstallments + count) - 1
                )
                listMovement.add(updateMovement)

            }
            repositoryMovement.updateMovements(listMovement)
        }

}