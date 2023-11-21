package com.gastometro.despesa.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gastometro.despesa.data.model.Category
import com.gastometro.despesa.data.model.CategorySpending
import com.gastometro.despesa.data.model.Movement
import kotlinx.coroutines.flow.Flow

@Dao
interface MovementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(movement: Movement)

    @Delete
    suspend fun delete(movement: Movement)

    @Update
    suspend fun update(movement: Movement)

    @Insert
    suspend fun insertAll(movement: List<Movement>)

    @Update
    suspend fun updateMovements(movement: List<Movement>)

    @Query("SELECT * FROM movement WHERE substr(date, 4, 7) = :stringForQuery ORDER BY movementId DESC")
    fun getMovementAmount(stringForQuery: String): Flow<List<Movement>>

    @Query("SELECT * FROM category WHERE name LIKE :name || '%'")
    fun searchCategoryName(name: String?): Flow<List<Category>>

    @Query("SELECT SUM(amount) FROM movement WHERE substr(date, 4, 7) = :monthFiltered")
    fun getAmountSpentOfMonth(monthFiltered: String): LiveData<Double>

    @Query("SELECT SUM(amount) FROM movement WHERE substr(date, 4, 7) = :monthFiltered AND paid = 1")
    fun getAmountPaidOfMonth(monthFiltered: String): LiveData<Double>

    @Query("SELECT SUM(amount) FROM movement WHERE substr(date, 4, 7) = :monthFiltered AND paid = 0")
    fun getAmountNotPaidOfMonth(monthFiltered: String): LiveData<Double>

    @Query("SELECT categoryName, SUM(amount) as totalAmount FROM movement WHERE substr(date, 4, 7) = :stringForQuery GROUP BY categoryName ORDER BY totalAmount DESC LIMIT 3")
    fun getTopSpendingCategories(stringForQuery: String): LiveData<List<CategorySpending>>

    @Query("DELETE FROM movement WHERE movementId BETWEEN :startId AND :endId")
    suspend fun excludeMovementsByInterval(startId: Int, endId: Int)

    @Query("SELECT COUNT(*) FROM movement WHERE (substr(date, 1, 10) = :today OR substr(date, 1, 10) = :tomorrow) AND paid = 0")
    fun getCountExpensesNotPaid(today: String, tomorrow: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM movement WHERE substr(date, 7, 4) < :year OR (substr(date, 7, 4) = :year AND substr(date, 4, 2) < :month) OR (substr(date, 7, 4) = :year AND substr(date, 4, 2) = :month AND substr(date, 1, 2) < :day) AND paid = 0")
    fun getCountExpensesOrverdue(day: String, month: String, year: String): LiveData<Int>


}