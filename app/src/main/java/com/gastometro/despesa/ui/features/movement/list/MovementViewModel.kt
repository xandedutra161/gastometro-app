package com.gastometro.despesa.ui.features.movement.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastometro.despesa.data.model.Movement
import com.gastometro.despesa.repositories.MovementRepository
import com.gastometro.despesa.ui.state.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovementViewModel @Inject constructor(
    private val repository: MovementRepository
) : ViewModel() {

    private val _movements =
        MutableStateFlow<ResourceState<List<Movement>>>(ResourceState.Empty())
    val movements: StateFlow<ResourceState<List<Movement>>> = _movements

    fun getMovementAmount(stringForQuery: String) = viewModelScope.launch {
        repository.getMovementAmount(stringForQuery).collect { movementsReceveid ->
            if (movementsReceveid.isNullOrEmpty()) {
                _movements.value = ResourceState.Empty()
            } else {
                _movements.value = ResourceState.Success(movementsReceveid)
            }
        }
    }

    fun delete(movement: Movement) = viewModelScope.launch {
        repository.delete(movement)
    }

    fun excludeMovementsByInterval(startId: Int, endId: Int) = viewModelScope.launch {
        repository.excludeMovementsByInterval(startId, endId)
    }

    fun getAmountSpentOfMonth(monthFiltered: String): LiveData<Double> {
        return repository.getAmountSpentOfMonth(monthFiltered)
    }

    fun getAmountPaidOfMonth(monthFiltered: String): LiveData<Double> {
        return repository.getAmountPaidOfMonth(monthFiltered)
    }

}

