package com.gastometro.despesa.util

import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import java.text.NumberFormat
import java.util.Locale

fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(
        requireContext(),
        message,
        duration
    ).show()
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.INVISIBLE
}

fun Fragment.convertDoubleInReal(amount: Double?): String {
    var textMonthFiltered = "R$ 0,00"
    if (amount != null) {
        val formatNumber = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        textMonthFiltered = formatNumber.format(amount)
    }
    return textMonthFiltered
}

fun Fragment.convertRealInDouble(amount: String): Double {
    var doubleAmount = amount.replace(Regex("[^0-9,]"), "").replace(",",".").toDouble()
    return doubleAmount
}

fun Fragment.assembleString(s: String): String {
    val parts = s.split(" ")
    var monthFiltered: String

    val monthNames = arrayOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    var m = monthNames.indexOf(parts[0])
    m++
    monthFiltered = if (m < 10) "0$m-" + parts[1] else "$m-" + parts[1]
    return monthFiltered
}

fun ViewModel.assembleString(s: String): String {
    val parts = s.split(" ")
    var monthFiltered: String

    val monthNames = arrayOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    var m = monthNames.indexOf(parts[0])
    m++
    monthFiltered = if (m < 10) "0$m-" + parts[1] else "$m-" + parts[1]
    return monthFiltered
}

fun Fragment.getMonthIndex(month: String): Int {
    val monthNames = arrayOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )
    return monthNames.indexOf(month)
}

fun Fragment.getMonthName(month: Int): String {
    val monthNames = arrayOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )
    return monthNames[month]
}

fun ViewModel.getMonthName(month: Int): String {
    val monthNames = arrayOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )
    return monthNames[month]
}

fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
    observeForever(object : Observer<T> {
        override fun onChanged(t: T) {
            observer.onChanged(t)
            removeObserver(this) // Remove automaticamente após a primeira emissão
        }
    })
}