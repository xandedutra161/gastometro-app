package com.gastometro.despesa.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.time.LocalDate

@Parcelize
@Entity(tableName = "movement")
data class Movement(
    @PrimaryKey(autoGenerate = true)
    val movementId: Int = 0,
    val amount: BigDecimal,
    val paid: Boolean = false,
    val description: String,
    val date: LocalDate,
    val categoryName: String,
    val fixed: Boolean,
    val installments: Boolean = false,
    val numberInstallments: Int = 0,
    val currentInstallments: Int = 0,
): Parcelable
