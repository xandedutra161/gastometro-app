package com.gastometro.despesa.ui.features.pay.list

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gastometro.despesa.R
import com.gastometro.despesa.data.model.Movement
import com.gastometro.despesa.databinding.FragmentPayBinding
import com.gastometro.despesa.di.module.AppModule
import com.gastometro.despesa.ui.adapters.PayAdapter
import com.gastometro.despesa.ui.base.BaseFragment
import com.gastometro.despesa.ui.state.ResourceState
import com.gastometro.despesa.util.assembleString
import com.gastometro.despesa.util.getMonthName
import com.gastometro.despesa.util.hide
import com.gastometro.despesa.util.show
import com.gastometro.despesa.util.toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class PayFragment : BaseFragment<FragmentPayBinding, PayViewModel>() {
    override val viewModel: PayViewModel by viewModels()
    private val payAdapter by lazy { PayAdapter() }

    private val fab: FloatingActionButton by lazy {
        requireActivity().findViewById(R.id.fab)
    }

    override fun getViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentPayBinding = FragmentPayBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerView()
        observer()
    }

    private fun setupViews() {
        clickFab()
        listenerBtnSelectDate()
    }

    private fun setupRecyclerView() {
        binding.rvPayments.apply {
            adapter = payAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun clickFab() {
        fab.setImageResource(R.drawable.baseline_price_check_24)

        fab.setOnClickListener {
            val listMovement = payAdapter.markedItems

            if (listMovement.isNotEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    for (movement in payAdapter.markedItems) {
                        val updateMovement = Movement(
                            movementId = movement.movementId,
                            amount = movement.amount,
                            paid = true,
                            description = movement.description,
                            date = movement.date,
                            categoryName = movement.categoryName,
                            fixed = movement.fixed,
                            installments = movement.installments,
                            numberInstallments = movement.numberInstallments,
                            currentInstallments = movement.currentInstallments,
                        )

                        viewModel.update(updateMovement)
                    }
                    payAdapter.markedItems.clear()

                    val selectedDate = binding.btnSelectDate.text.toString()
                    val action = PayFragmentDirections.actionPayFragmentToPaymentFragment(selectedDate)
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun listenerBtnSelectDate() {
        val monthCurrent = getMonthName(AppModule.calendar.get(Calendar.MONTH))
        val yearCurrent = AppModule.calendar.get(Calendar.YEAR)
        val selectedDate = "$monthCurrent $yearCurrent"
        val receivedText = arguments?.getString("stringPay")

        if(receivedText.isNullOrEmpty()){
            binding.btnSelectDate.text =
                selectedDate
        }

        receivedText?.let {
            binding.btnSelectDate.text = it
        }

        findMonth(binding.btnSelectDate.text.toString())

        binding.btnSelectDate.setOnClickListener {
            showMonthYearPickerDialog(requireContext()) { selectedDate ->
                findMonth(selectedDate)
            }
        }
    }

    private fun findMonth(selectedDate: String) {
        binding.btnSelectDate.text = selectedDate
        val monthFiltered = assembleString(selectedDate)
        lifecycleScope.launch {
            viewModel.getMovementAmount(monthFiltered)
        }

    }

    private fun showMonthYearPickerDialog(context: Context, onDateSelected: (String) -> Unit) {
        val currentMonth = AppModule.calendar.get(Calendar.MONTH)
        val currentYear = AppModule.calendar.get(Calendar.YEAR)

        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_month_year_picker, null)
        val monthPicker = view?.findViewById<NumberPicker>(R.id.monthPicker)
        val yearPicker = view?.findViewById<NumberPicker>(R.id.yearPicker)

        val monthNames = context.resources.getStringArray(R.array.months)

        monthPicker?.minValue = 0
        monthPicker?.maxValue = monthNames.size - 1
        monthPicker?.displayedValues = monthNames
        monthPicker?.value = currentMonth

        yearPicker?.minValue = 2000
        yearPicker?.maxValue = 2100
        yearPicker?.value = currentYear

        val dialog = AlertDialog.Builder(context)
            .setTitle("Selecionar Mês e Ano")
            .setView(view)
            .setPositiveButton("Ok") { _, _ ->
                val selectedMonth = monthNames[monthPicker?.value ?: 0]
                val selectedYear = yearPicker?.value ?: currentYear
                val selectedDate = "$selectedMonth $selectedYear"
                onDateSelected(selectedDate)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun observer() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movements.collect { resource ->
                when (resource) {
                    is ResourceState.Success -> {
                        resource.data?.let {
                            binding.tvEmptyListPay.hide()
                            payAdapter.movements = it.toList()
                        }
                    }

                    is ResourceState.Error -> {
                        toast("Ocorreu um erro")
                    }

                    is ResourceState.Empty -> {
                        binding.tvEmptyListPay.show()
                        payAdapter.movements = emptyList()
                    }

                    else -> {
                        // Handle other states if needed
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fab.setImageResource(R.drawable.baseline_add_24)
    }

    override fun onPause() {
        super.onPause()
        arguments?.putString("stringPay", null)
    }

}
