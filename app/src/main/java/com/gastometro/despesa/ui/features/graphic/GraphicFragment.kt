package com.gastometro.despesa.ui.features.graphic

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.viewModels
import com.gastometro.despesa.R
import com.gastometro.despesa.databinding.FragmentGraphicBinding
import com.gastometro.despesa.ui.base.BaseFragment
import com.gastometro.despesa.util.Constants.MAX_YEAR
import com.gastometro.despesa.util.Constants.MIN_YEAR
import com.gastometro.despesa.util.assembleString
import com.gastometro.despesa.util.convertDoubleInReal
import com.gastometro.despesa.util.getMonthName
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class GraphicFragment : BaseFragment<FragmentGraphicBinding, GraphicViewModel>() {
    override val viewModel: GraphicViewModel by viewModels()
    private val calendar = Calendar.getInstance()
    private val fab: FloatingActionButton by lazy {
        requireActivity().findViewById(R.id.fab)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGraphicBinding =
        FragmentGraphicBinding.inflate(inflater, container, false)

    private fun setupUI() {
        configureFab()
        configureBtnSelectDate()
        observeData()
        //viewModel.getTopSpendingCategories(assembleString(binding.btnSelectDate.text.toString()))
    }

    private fun configureFab() {
        fab.hide()
        fab.setOnClickListener {
            // Ação do FloatingActionButton
        }
    }

    private fun configureBtnSelectDate() {
        val monthCurrent = getMonthName(calendar.get(Calendar.MONTH))
        val yearCurrent = calendar.get(Calendar.YEAR)
        val selectedDate = "$monthCurrent $yearCurrent"
        binding.btnSelectDate.text = selectedDate

        binding.btnSelectDate.setOnClickListener {
            showMonthYearPickerDialog(requireContext()) { selectedDate ->
                binding.btnSelectDate.text = selectedDate

                var stringBtnSelect = binding.btnSelectDate.text.toString()
                stringBtnSelect = assembleString(stringBtnSelect)
                viewModel.setMonthAndYear(stringBtnSelect)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeData() {
        var totalAmount: Double? = 0.00

        viewModel.amountTotalMonth.observe(viewLifecycleOwner) { resultAmountTotal ->
            if (resultAmountTotal != null) {
                totalAmount = resultAmountTotal
                viewModel.categorySpendings.observe(
                    viewLifecycleOwner
                ) { obsCategorySpending ->
                    if (!obsCategorySpending.isNullOrEmpty()) {
                        resetLabels()
                        val entries = mutableListOf<PieEntry>()
                        var threeExpenses = 0.0
                        for ((index, categorySpending) in obsCategorySpending.withIndex()) {
                            val categoryName = categorySpending.categoryName
                            val amountCategory = categorySpending.totalAmount
                            val categoryPercentage =
                                calculatePercent(amountCategory.toDouble(), totalAmount!!)

                            val decimalFloat = (categoryPercentage * 100.0f).toInt() / 100.0f

                            if (index < 3) {
                                threeExpenses += amountCategory.toDouble()
                                setCategoryLabel(
                                    index,
                                    categoryName,
                                    amountCategory.toDouble(),
                                    decimalFloat
                                )
                            }

                            entries.add(PieEntry(decimalFloat, categoryName))
                        }

                        if (threeExpenses != totalAmount) {
                            val otherCategory = totalAmount!! - threeExpenses
                            setupCategoryOther(otherCategory, totalAmount!!, entries)
                        }

                        binding.tvTotal.text = "Total (${convertDoubleInReal(totalAmount!!)})"
                        binding.tvTotalPercent.text = "100%"

                        updatePieChart(entries)
                    }
                }
            } else {
                resetLabels()
            }
        }


        var stringBtnSelect = binding.btnSelectDate.text.toString()
        stringBtnSelect = assembleString(stringBtnSelect)
        viewModel.setMonthAndYear(stringBtnSelect)

    }

    @SuppressLint("SetTextI18n")
    private fun setCategoryLabel(
        index: Int,
        categoryName: String,
        amount: Double,
        percentage: Float
    ) {
        when (index) {
            0 -> {
                binding.category1.text = "$categoryName (${convertDoubleInReal(amount)})"
                binding.percent1.text = "$percentage%"
            }

            1 -> {
                binding.category2.text = "$categoryName (${convertDoubleInReal(amount)})"
                binding.percent2.text = "$percentage%"
            }

            2 -> {
                binding.category3.text = "$categoryName (${convertDoubleInReal(amount)})"
                binding.percent3.text = "$percentage%"
            }
        }
    }

    private fun resetLabels() {
        val emptyLabel = getString(R.string.empt_graphic)
        binding.category1.text = emptyLabel
        binding.category2.text = emptyLabel
        binding.category3.text = emptyLabel
        binding.categoryOthers.text = emptyLabel
        binding.tvTotal.text = emptyLabel
        binding.percent1.text = "0%"
        binding.percent2.text = "0%"
        binding.percent3.text = "0%"
        binding.percentOthers.text = "0%"
        binding.tvTotalPercent.text = "0%"

        // Remove todas as entradas do gráfico
        updatePieChart(emptyList())

    }

    @SuppressLint("SetTextI18n")
    private fun setupCategoryOther(
        otherCategory: Double,
        totalExpenses: Double,
        entries: MutableList<PieEntry>
    ) {
        val otherCategoryPercent = calculatePercent(otherCategory, totalExpenses)
        val decimalFloat = (otherCategoryPercent * 100.0f).toInt() / 100.0f
        entries.add(PieEntry(decimalFloat, getString(R.string.category_others)))
        binding.categoryOthers.text =
            "${getString(R.string.category_others)} (${convertDoubleInReal(otherCategory)})"
        binding.percentOthers.text = "$decimalFloat%"
    }

    private fun calculatePercent(smallValue: Double, valueTotal: Double): Double {
        return (smallValue / valueTotal) * 100.0
    }

    private fun updatePieChart(entries: List<PieEntry>) {
        val pieChart = binding.pieChart
        pieChart.setHoleColor(resources.getColor(R.color.backgroundColor))
        val dataSet = PieDataSet(entries, "")
        dataSet.setColors(
            intArrayOf(
                R.color.blue,
                R.color.green,
                R.color.orange,
                R.color.purple
            ), context
        )

        val colors = ArrayList<Int>()
        colors.add(Color.WHITE) // Cor para o primeiro valor
        dataSet.setValueTextColors(colors)
        val data = PieData(dataSet)
        data.setValueTextSize(18f)

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.legend.textColor = resources.getColor(R.color.fontColor)
        pieChart.animateY(1000)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showMonthYearPickerDialog(context: Context, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)

        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = view?.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view?.findViewById<NumberPicker>(R.id.yearPicker)

        val monthNames = context.resources.getStringArray(R.array.months)

        monthPicker?.minValue = 0
        monthPicker?.maxValue = monthNames.size - 1
        monthPicker?.displayedValues = monthNames
        monthPicker?.value = currentMonth

        yearPicker?.minValue = MIN_YEAR
        yearPicker?.maxValue = MAX_YEAR
        yearPicker?.value = currentYear

        val dialog = AlertDialog.Builder(context)
            .setTitle("Selecionar Mês e Ano")
            .setView(view)
            .setPositiveButton("Ok") { _, _ ->
                val selectedMonth = monthNames[monthPicker?.value ?: 0]
                val selectedYear = yearPicker?.value ?: currentYear
                val selectedDate = "$selectedMonth $selectedYear"
                // Executa a ação em uma corrotina em segundo plano
                GlobalScope.launch(Dispatchers.Main) {
                    onDateSelected(selectedDate)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        fab.hide()
    }

    override fun onPause() {
        super.onPause()
        fab.show()
    }

}
