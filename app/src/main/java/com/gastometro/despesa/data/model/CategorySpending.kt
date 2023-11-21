package com.gastometro.despesa.data.model

import java.math.BigDecimal

data class CategorySpending(
    val categoryName: String,
    val totalAmount: BigDecimal
)