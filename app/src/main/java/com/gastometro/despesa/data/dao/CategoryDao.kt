package com.gastometro.despesa.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gastometro.despesa.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Query("SELECT * FROM category ORDER BY categoryId DESC")
    fun getAll(): Flow<List<Category>>

    @Delete
    suspend fun delete(category: Category)

    @Update
    suspend fun update(category: Category)

    @Insert
    fun insertAll(category: List<Category>)

    @Query("SELECT COUNT(*) FROM category WHERE name = :name")
    suspend fun isCategoryExists(name: String): Boolean

}