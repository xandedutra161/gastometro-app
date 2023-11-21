package com.gastometro.despesa.ui.features.pay.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastometro.despesa.data.model.Movement
import com.gastometro.despesa.repositories.MovementRepository
import com.gastometro.despesa.ui.state.ResourceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PayViewModel @Inject constructor(
    private val repository: MovementRepository
): ViewModel(){
    private val _movements =
        MutableStateFlow<ResourceState<List<Movement>>>(ResourceState.Empty())
    val movements: StateFlow<ResourceState<List<Movement>>> = _movements

    fun getMovementAmount(stringForQuery: String) = viewModelScope.launch {
        repository.getMovementAmount(stringForQuery).collectLatest {
            if (it.isNullOrEmpty()) {
                _movements.value = ResourceState.Empty()
            } else {
                _movements.value = ResourceState.Success(it)
            }

        }
    }

    suspend fun update(movement: Movement) = viewModelScope.launch {
        repository.update(movement)
    }

}