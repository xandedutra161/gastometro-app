package com.gastometro.despesa.ui.features.movement.list

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gastometro.despesa.R
import com.gastometro.despesa.data.model.Movement
import com.gastometro.despesa.databinding.FragmentMovementBinding
import com.gastometro.despesa.di.module.AppModule.calendar
import com.gastometro.despesa.ui.adapters.MovementAdapter
import com.gastometro.despesa.ui.base.BaseFragment
import com.gastometro.despesa.ui.state.ResourceState
import com.gastometro.despesa.util.assembleString
import com.gastometro.despesa.util.convertDoubleInReal
import com.gastometro.despesa.util.convertRealInDouble
import com.gastometro.despesa.util.getMonthIndex
import com.gastometro.despesa.util.getMonthName
import com.gastometro.despesa.util.hide
import com.gastometro.despesa.util.show
import com.gastometro.despesa.util.toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class MovementFragment : BaseFragment<FragmentMovementBinding, MovementViewModel>() {
    override val viewModel: MovementViewModel by viewModels()
    private val movementAdapter by lazy { MovementAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        listenerMonth()
        clickFab()
        clickAdapter()
        clickPopupMenu()
        observer()
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMovementBinding =
        FragmentMovementBinding.inflate(inflater, container, false)

    private fun setupRecycleView() = with(binding) {
        rvMovements.apply {
            adapter = movementAdapter
            layoutManager = LinearLayoutManager(context)
        }
        ItemTouchHelper(itemTouchHelperCallback()).attachToRecyclerView(rvMovements)
    }

    private fun itemTouchHelperCallback(): ItemTouchHelper.SimpleCallback {
        return object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val movement = movementAdapter.getMovementPosition(viewHolder.adapterPosition)
                viewModel.delete(movement).also {
                    toast(getString(R.string.message_delete_category))
                    val selectedDate = binding.tvMonth.text.toString()
                    findMonth(selectedDate)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun listenerMonth() {
        val monthCurrent = getMonthName(calendar.get(Calendar.MONTH))
        val yearCurrent = calendar.get(Calendar.YEAR)
        val args = MovementFragmentArgs.fromBundle(requireArguments())
        val argMonth = args.month

        if(argMonth.isNullOrEmpty()){
            binding.tvMonth.text =
                "$monthCurrent $yearCurrent"
        }

        argMonth?.let {
            binding.tvMonth.text = it
        }

        val selectedDate = binding.tvMonth.text.toString()
        findMonth(selectedDate)

        updateMonthExpenseTotal(assembleString(binding.tvMonth.text.toString()))

        binding.tvMonth.setOnClickListener {
            showMonthYearPickerDialog(requireContext()) { selectedDate ->
                binding.tvMonth.text = selectedDate
                findMonth(selectedDate)
            }
        }

        binding.ibLeftMonth.setOnClickListener {
            monthBefore()
        }

        binding.ibRightMonth.setOnClickListener {
            monthNext()
        }

        binding.tvMonth.addTextChangedListener(afterTextChanged = { s ->
            val monthFiltered = assembleString(s.toString())
            viewModel.getMovementAmount(monthFiltered)
            updateMonthExpenseTotal(monthFiltered)
        })
    }

    private fun clickPopupMenu() {
        movementAdapter.setOnMenuItemClickListener { result ->
            if (result.second!!.isNotEmpty()) {
                val btnType = result.second
                if (btnType == "edit") {
                    val c = result.first

                    val selectedDate = binding.tvMonth.text.toString()

                    val action =
                        MovementFragmentDirections.actionHomeFragmentToRegisterMovementFragment(
                            c,
                            selectedDate
                        )
                    findNavController().navigate(action)
                }
                if (btnType == "del") {
                    val c = result.first
                    if (c != null) {
                        if (c.numberInstallments > 0) {
                            displayDialogBoxExclusion(c)
                        } else {
                            viewModel.delete(c).also {
                                val selectedDate = binding.tvMonth.text.toString()
                                findMonth(selectedDate)
                                toast(getString(R.string.message_delete_category))
                            }
                        }
                    }

                }
            }
        }
    }

    private fun showMonthYearPickerDialog(context: Context, onDateSelected: (String) -> Unit) {
        val dataString = assembleString(binding.tvMonth.text.toString())
        val splitDate = dataString.split("-")

        val currentMonth = splitDate[0].toInt() - 1
        val currentYear = splitDate[1].toInt()

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

    private fun clickAdapter() {
        movementAdapter.setOnClickListener { movement ->
            val selectedDate = binding.tvMonth.text.toString()
            val action =
                MovementFragmentDirections.actionHomeFragmentToRegisterMovementFragment(
                    movement,
                    selectedDate
                )
            findNavController().navigate(action)
        }
    }

    private fun observer() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movements.collect { resource ->
                when (resource) {
                    is ResourceState.Success -> {
                        resource.data?.let {
                            binding.tvEmptyListMov.hide()
                            movementAdapter.movements = it.toList()
                        }
                    }

                    is ResourceState.Error -> {
                        toast("teste deu algum erro")
                    }

                    is ResourceState.Empty -> {
                        binding.tvEmptyListMov.show()
                        movementAdapter.movements = emptyList()
                    }

                    else -> {
                    }
                }
            }
        }
    }

    private fun displayDialogBoxExclusion(movement: Movement) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog_layout, null)

        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        val current = dialogView.findViewById<Button>(R.id.btnCurrent)
        val all = dialogView.findViewById<Button>(R.id.btnAll)
        val currentAndNext = dialogView.findViewById<Button>(R.id.btnCurrentAndNext)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        current.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                dialog.dismiss()
                viewModel.delete(movement)
                val selectedDate = binding.tvMonth.text.toString()
                findMonth(selectedDate)
            }

        }

        all.setOnClickListener {
            val startId = (movement.movementId - movement.currentInstallments) + 1
            val endId =
                ((movement.numberInstallments - movement.currentInstallments) + movement.movementId)

            viewLifecycleOwner.lifecycleScope.launch {
                dialog.dismiss()
                viewModel.excludeMovementsByInterval(startId, endId)
                val selectedDate = binding.tvMonth.text.toString()
                findMonth(selectedDate)

            }

        }

        currentAndNext.setOnClickListener {
            val startId = movement.movementId
            val endId =
                ((movement.numberInstallments - movement.currentInstallments) + movement.movementId)

            viewLifecycleOwner.lifecycleScope.launch {
                dialog.dismiss()
                viewModel.excludeMovementsByInterval(startId, endId)
                val selectedDate = binding.tvMonth.text.toString()
                findMonth(selectedDate)
            }

        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

    }

    @SuppressLint("ResourceType")
    private fun clickFab() {
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)

        fab.setOnClickListener {
            val selectedDate = binding.tvMonth.text.toString()
            val action =
                MovementFragmentDirections.actionHomeFragmentToRegisterMovementFragment(month = selectedDate)
            findNavController().navigate(action)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun findMonth(selectedDate: String) {
        val monthFiltered = assembleString(selectedDate)
        viewModel.getMovementAmount(monthFiltered)
        movementAdapter.notifyDataSetChanged()
    }

    private fun updateMonthExpenseTotal(monthFiltered: String) {
        viewModel.getAmountSpentOfMonth(monthFiltered)
            .observe(viewLifecycleOwner) { amount ->
                binding.tvMonthExpense.text = convertDoubleInReal(amount)
                updateMonthExpensePaid(monthFiltered)
            }
    }

    private fun updateMonthExpensePaid(monthFiltered: String) {
        viewModel.getAmountPaidOfMonth(monthFiltered)
            .observe(viewLifecycleOwner) { amount ->
                binding.tvPaid.text = convertDoubleInReal(amount)
                updateMonthExpenseNotPaid()
            }
    }

    private fun updateMonthExpenseNotPaid() {
        val monthTotalStr = binding.tvMonthExpense.text.toString()
        val monthPaidStr = binding.tvPaid.text.toString()

        val monthTotalDouble = convertRealInDouble(monthTotalStr)
        val monthPaidDouble = convertRealInDouble(monthPaidStr)

        val monthExpense = monthTotalDouble - monthPaidDouble
        binding.tvExpenseRemaining.text = convertDoubleInReal(monthExpense)
    }


    private fun monthBefore() {
        val monthText = binding.tvMonth.text.toString()
        val parts = monthText.split(" ")

        var m = getMonthIndex(parts[0])
        var y = parts[1].toInt()

        if (m == 0) {
            m = 11
            y--
        } else {
            m--
        }

        val stringMonth = getMonthName(m) + " $y"
        binding.tvMonth.text = stringMonth
    }

    private fun monthNext() {
        val monthText = binding.tvMonth.text.toString()
        val parts = monthText.split(" ")

        var m = getMonthIndex(parts[0])
        var y = parts[1].toInt()

        if (m == 11) {
            m = 0
            y++
        } else {
            m++
        }

        val stringMonth = getMonthName(m) + " $y"
        binding.tvMonth.text = stringMonth
    }

    override fun onPause() {
        super.onPause()
        arguments?.putString("month", null)
    }

}