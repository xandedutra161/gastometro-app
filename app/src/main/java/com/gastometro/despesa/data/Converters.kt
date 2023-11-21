package com.gastometro.despesa.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): Double? {
        return value?.toDouble()
    }

    @TypeConverter
    fun toBigDecimal(value: Double?): BigDecimal? {
        return value?.toBigDecimal()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromDate(value: LocalDate?): String? {
        return value?.format(dateFormatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }

}