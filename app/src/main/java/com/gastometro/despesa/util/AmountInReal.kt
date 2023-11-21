package com.gastometro.despesa.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.NumberFormat
import java.util.Locale

class AmountInReal(private val editText: EditText) : TextWatcher {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    init {
        editText.addTextChangedListener(this)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        editText.removeTextChangedListener(this)

        val cleanString = s.toString().replace("[R$,.\\s]".toRegex(), "")
        val parsed = cleanString.toDoubleOrNull() ?: 0.0

        val formatted = currencyFormat.format(parsed / 100)
        editText.setText(formatted)
        editText.setSelection(formatted.length)

        editText.addTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable?) {}
}

