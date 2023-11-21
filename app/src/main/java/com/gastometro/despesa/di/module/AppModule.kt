package com.gastometro.despesa.di.module

import android.content.Context
import androidx.room.Room
import com.gastometro.despesa.data.db.AppDatabase
import com.gastometro.despesa.util.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Calendar
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        DATABASE_NAME
    ).createFromAsset("database/category.db").build()
    //).createFromAsset("database/category.db").addTypeConverter(Converters::class).build()

    @Singleton
    @Provides
    fun providerCategoryDao(database: AppDatabase) = database.categoryDao()

    @Singleton
    @Provides
    fun providerMovementDao(database: AppDatabase) = database.movementDao()

    @Singleton
    val calendar: Calendar = Calendar.getInstance()

}