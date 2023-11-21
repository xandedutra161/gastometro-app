package com.gastometro.despesa.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gastometro.despesa.data.Converters
import com.gastometro.despesa.data.dao.CategoryDao
import com.gastometro.despesa.data.dao.MovementDao
import com.gastometro.despesa.data.model.Category
import com.gastometro.despesa.data.model.Movement

@Database(entities = [Category::class, Movement::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun movementDao(): MovementDao
}