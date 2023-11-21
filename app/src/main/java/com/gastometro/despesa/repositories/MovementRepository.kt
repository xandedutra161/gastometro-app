package com.gastometro.despesa.repositories

import androidx.lifecycle.LiveData
import com.gastometro.despesa.data.dao.MovementDao
import com.gastometro.despesa.data.model.CategorySpending
import com.gastometro.despesa.data.model.Movement
import javax.inject.Inject

class MovementRepository @Inject constructor(
    private val dao: MovementDao
){
    suspend fun update(movement: Movement) = dao.update(movement)
    suspend fun delete(movement: Movement) = dao.delete(movement)
    suspend fun insert(movement: Movement) = dao.insert(movement)
    suspend fun insertAll(movement: List<Movement>) = dao.insertAll(movement)
    suspend fun updateMovements(movement: List<Movement>) = dao.updateMovements(movement)
    suspend fun excludeMovementsByInterval(startId: Int, endId: Int) = dao.excludeMovementsByInterval(startId, endId)
    fun getMovementAmount(stringForQuery: String) = dao.getMovementAmount(stringForQuery)
    fun searchCategoryName(name: String) = dao.searchCategoryName(name)
    fun getAmountSpentOfMonth(monthFiltered: String) = dao.getAmountSpentOfMonth(monthFiltered)
    fun getAmountPaidOfMonth(monthFiltered: String) = dao.getAmountPaidOfMonth(monthFiltered)
    fun getTopSpendingCategories(stringForQuery: String): LiveData<List<CategorySpending>> = dao.getTopSpendingCategories(stringForQuery)

}