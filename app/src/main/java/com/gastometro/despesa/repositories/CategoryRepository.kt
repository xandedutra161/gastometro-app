package com.gastometro.despesa.repositories

import com.gastometro.despesa.data.dao.CategoryDao
import com.gastometro.despesa.data.model.Category
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val dao: CategoryDao
) {

    suspend fun update(category: Category) = dao.update(category)
    fun getAll() = dao.getAll()
    suspend fun delete(category: Category) = dao.delete(category)
    suspend fun insert(category: Category) = dao.insert(category)
    suspend fun isCategoryExists(name: String): Boolean {
        return dao.isCategoryExists(name)
    }


}
