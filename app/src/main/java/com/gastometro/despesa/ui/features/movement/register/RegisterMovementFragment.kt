package com.gastometro.despesa.ui.features.movement.register

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gastometro.despesa.R
import com.gastometro.despesa.data.model.Movement
import com.gastometro.despesa.databinding.FragmentRegisterMovementBinding
import com.gastometro.despesa.di.module.AppModule.calendar
import com.gastometro.despesa.ui.activities.MainActivity
import com.gastometro.despesa.ui.base.BaseFragment
import com.gastometro.despesa.util.AmountInReal
import com.gastometro.despesa.util.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class RegisterMovementFragment :
    BaseFragment<FragmentRegisterMovementBinding, RegisterMovementViewModel>(){
    override val viewModel: RegisterMovementViewModel by viewModels()
    private lateinit var amountInReal: AmountInReal
    private lateinit var dateEditText: TextInputEditText
    private val args: RegisterMovementFragmentArgs by navArgs()
    private var updateMovement = false

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRegisterMovementBinding =
        FragmentRegisterMovementBinding.inflate(inflater, container, false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUI(view: View) {
        resetFields()
        args.movement?.categoryName?.let { updateFields() }
        hideNavigation()
        showOrHideHint()
        maskAmount()
        listeners()
        observerBtnCategory()
        clickBtn()
    }

    private fun listeners() {
        cleanHelpers()
        listenerDate()
        listenerRadioButtons()
    }

    private fun cleanHelpers() = with(binding){
        clearHelperText(etDate, tilDate)
        clearHelperText(etDescription, tilDescription)
        clearHelperText(etAmount, tilAmount)
        clearHelperText(etNumberInstallments, tilNumberInstallments)
    }

    private fun clearHelperText(editText: EditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener { s ->
            textInputLayout.helperText = if (s.isNullOrEmpty()) null else ""
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun clickBtn() {
        clickBtnCategory()
        clickBtnSave()
        clickBtnBack()
    }

    private fun listenerRadioButtons() = with(binding) {
        rbPaid.setOnClickListener {
            rbPaid.isChecked = true
            rbNotPaid.isChecked = false
        }

        rbNotPaid.setOnClickListener {
            rbPaid.isChecked = false
            rbNotPaid.isChecked = true
        }

        var isRepeatSelected = false

        rbRepeat.setOnClickListener {
            isRepeatSelected = !isRepeatSelected
            rbRepeat.isChecked = isRepeatSelected

            rbFixed.isVisible = isRepeatSelected
            rbInstallments.isVisible = isRepeatSelected
            tilNumberInstallments.isVisible = isRepeatSelected

            if (isRepeatSelected) {
                rbInstallments.isChecked = true
                rbFixed.isChecked = false
                tilNumberInstallments.isVisible = true
            } else {
                etNumberInstallments.setText("")
            }
        }

        rbInstallments.setOnClickListener {
            rbInstallments.isChecked = true
            rbFixed.isChecked = false
            tilNumberInstallments.isVisible = true
        }

        rbFixed.setOnClickListener {
            rbFixed.isChecked = true
            rbInstallments.isChecked = false
            tilNumberInstallments.isInvisible = true
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateFields() {
        updateMovement = true

        binding.btnSaveMovement.text = getString(R.string.changeBtnSave)
        val date = args.movement!!.date
        val category = args.movement!!.categoryName
        val amount = args.movement!!.amount
        val description = args.movement!!.description
        val paid = args.movement!!.paid
        val fixed = args.movement!!.fixed
        val installments = args.movement!!.installments
        val numberInstallments = args.movement!!.numberInstallments

        binding.btnCategory.text = category
        binding.etDescription.setText(description)
        binding.etNumberInstallments.setText(numberInstallments.toString())

        binding.rbRepeat.isInvisible = true

        if (installments) {
            binding.rbInstallments.isChecked = true
            binding.rbFixed.isChecked = false
        }
        if (fixed) {
            binding.rbFixed.isChecked = true
            binding.rbInstallments.isChecked = false
        }

        if (paid) {
            binding.rbPaid.isChecked = true
            binding.rbNotPaid.isChecked = false
        } else {
            binding.rbPaid.isChecked = false
            binding.rbNotPaid.isChecked = true
        }

        val formatNumber = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val amountFormated = formatNumber.format(amount)
        binding.etAmount.setText(amountFormated)

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val dateFormated = dateFormatter.format(date)
        binding.etDate.setText(dateFormated)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun clickBtnSave() = with(binding) {
        binding.btnSaveMovement.setOnClickListener {
            val date = etDate.text.toString()
            val amount = etAmount.text.toString()
            val description = etDescription.text.toString()
            val category = btnCategory.text.toString()
            val paid = rbPaid.isChecked
            val fixed = rbFixed.isChecked
            val installments = rbInstallments.isChecked
            var numberInstallments = etNumberInstallments.text.toString().toIntOrNull() ?: 0

            val amountBigDecimal =
                amount.replace(Regex("[^0-9,]"), "").replace(",", ".").toBigDecimal()

            if (!validateFields(amountBigDecimal, description, date, category)) {
                return@setOnClickListener
            }
            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            try {
                val dateDate = LocalDate.parse(
                    date,
                    dateFormatter
                ) // Converter a data diretamente para LocalDate
                val movement = if (updateMovement) {
                    Movement(
                        movementId = args.movement!!.movementId,
                        amount = amountBigDecimal,
                        paid = paid,
                        description = description,
                        date = dateDate,
                        categoryName = category,
                        fixed = fixed,
                        installments = installments,
                        numberInstallments = numberInstallments,
                        currentInstallments = args.movement!!.currentInstallments
                    )
                } else {
                    if (fixed) {
                        numberInstallments = calculateInstallmentsSize(dateDate)
                    }
                    Movement(
                        amount = amountBigDecimal,
                        paid = paid,
                        description = description,
                        date = dateDate,
                        categoryName = category,
                        fixed = fixed,
                        installments = installments,
                        numberInstallments = numberInstallments,
                    )

                }

                if (updateMovement) {
                    if (movement.numberInstallments > 0) {
                        displayDialogBoxUpdate(movement)
                    } else {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.update(movement)
                            toast(getString(R.string.message_update_movement))
                            showNavigation()
                            goToFragmentHome()
                        }
                    }
                } else {
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.insert(movement)
                        toast(getString(R.string.message_register_movement))
                        showNavigation()
                        goToFragmentHome()
                    }
                }



            } catch (e: DateTimeParseException) {
                // Trate o erro de formato de data aqui, por exemplo, exibindo uma mensagem de erro para o usuário.
                toast("Algum erro na data DateTimeParseException")
            }

        }
    }


    private fun goToFragmentHome(){
        showNavigation()
        val selectedDate = args.month!!

        val action = RegisterMovementFragmentDirections.actionRegisterMovementFragmentToHomeFragment(selectedDate)
        findNavController().navigate(action)
    }

    @SuppressLint("SetTextI18n")
    private fun resetFields() = with(binding) {
        etAmount.setText("R$ 0,00")
        etDescription.setText("")
        etDate.setText("")
        etNumberInstallments.setText("")

        rbRepeat.isChecked = false
        rbFixed.isChecked = false
        rbInstallments.isChecked = false
        rbInstallments.isInvisible = true
        rbFixed.isInvisible = true
        tilNumberInstallments.isInvisible = true
        rbPaid.isChecked = false
        rbNotPaid.isChecked = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayDialogBoxUpdate(movement: Movement) {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_dialog_layout, null)

        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        val current = dialogView.findViewById<Button>(R.id.btnCurrent)
        val all = dialogView.findViewById<Button>(R.id.btnAll)
        val currentAndNext = dialogView.findViewById<Button>(R.id.btnCurrentAndNext)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val textView = dialogView.findViewById<TextView>(R.id.dialog_message)

        textView.setText(R.string.dialog_message_update)

        current.setOnClickListener {
            dialog.dismiss()
            updateCurrentParcelInUI(movement)
        }

        all.setOnClickListener {
            dialog.dismiss()
            updateAllParcels(movement)
        }

        currentAndNext.setOnClickListener {
            dialog.dismiss()
            updateCurrentAndSubsequentParcels(movement)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun updateCurrentParcelInUI(movement: Movement) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.update(movement)
            goToFragmentHome()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAllParcels(movement: Movement) {
        viewLifecycleOwner.lifecycleScope.launch {
            val startId = (movement.movementId - movement.currentInstallments) + 1
            val endId =
                ((movement.numberInstallments - movement.currentInstallments) + movement.movementId)

            viewModel.updateAllParcels(startId, endId, movement)
            goToFragmentHome()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCurrentAndSubsequentParcels(movement: Movement) {
        viewLifecycleOwner.lifecycleScope.launch {
            val startId = movement.movementId
            val endId =
                ((movement.numberInstallments - movement.currentInstallments) + movement.movementId)

            viewModel.updateCurrentAndSubsequentParcels(startId, endId, movement)
            goToFragmentHome()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateInstallmentsSize(date: LocalDate): Int {
        val december2010 = LocalDate.of(2101, Month.JANUARY, 31)

        // Calcular a diferença entre hoje e dezembro de 2100 em meses
        return ChronoUnit.MONTHS.between(date, december2010).toInt()
    }

    private fun validateFields(
        amountBigDecimal: BigDecimal,
        description: String,
        date: String,
        category: String,
    ): Boolean {
        var counter = 0
        var sucess = true

        binding.tilAmount.helperText = ""
        binding.tilDescription.helperText = ""
        binding.tilDate.helperText = ""
        binding.tilNumberInstallments.helperText = ""

        if (amountBigDecimal <= BigDecimal.ZERO) {
            binding.tilAmount.helperText = "Informar valor."
            counter++
        }
        if (description.isEmpty()) {
            binding.tilDescription.helperText = "Informar descrição."
            counter++
        }
        if (date.isEmpty() || date.length != 10) {
            binding.tilDate.helperText = "Informar data \"dd/MM/yyyy\"."
            counter++
        }

        val defaultBtnName = getString(R.string.select_category)
        if (category == defaultBtnName) {
            toast("Selecionar Categoria")
            counter++
        }

        if (counter > 0) sucess = false

        if (binding.rbInstallments.isChecked && binding.rbRepeat.isChecked &&
            (binding.etNumberInstallments.text.toString()
                .isEmpty() || binding.etNumberInstallments.text.toString().toInt() < 1)
        ) {
            sucess = false
            binding.tilNumberInstallments.helperText = "Informar parcelas"
        }

        return sucess
    }


    private fun clickBtnCategory() {
        binding.btnCategory.setOnClickListener {

            val bottomSheetFragment = BottomSheetCategoriesFragment(viewModel)
            bottomSheetFragment.setStyle(
                DialogFragment.STYLE_NO_FRAME,
                R.style.BottomSheetDialogTheme
            )
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }

    private fun observerBtnCategory() {
        viewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            binding.btnCategory.text = category
        }
    }

    private fun listenerDate() {
        dateEditText = binding.etDate
        dateEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // TODO
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // TODO
            }

            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()
                if (inputText.length == 2 && !inputText.contains("/")) {
                    val formatDate = validateDate(inputText, 'd')
                    dateEditText.setText(formatDate)
                    dateEditText.setSelection(dateEditText.text!!.length)
                } else if (inputText.length == 5 && !inputText.substring(3, 5).contains("/")) {
                    val newText = StringBuilder(inputText).insert(5, "/").toString()
                    val formatDate = validateDate(newText, 'm')
                    dateEditText.setText(formatDate)
                    dateEditText.setSelection(dateEditText.text!!.length)

                } else if (inputText.length == 10 && !inputText.substring(3, 5).contains("/")) {
                    val formatDate = validateDate(inputText, 'a')
                    dateEditText.removeTextChangedListener(this)
                    dateEditText.setText(formatDate)
                    dateEditText.setSelection(dateEditText.text!!.length)
                    dateEditText.addTextChangedListener(this)
                }
            }
        })

        dateEditText.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.action == android.view.KeyEvent.ACTION_DOWN) {
                val currentCursorPosition = dateEditText.selectionStart
                val inputText = dateEditText.text.toString()
                if (currentCursorPosition in 4..6) {
                    if (inputText[currentCursorPosition - 1] == '/') {
                        val newText = inputText.dropLast(2)
                        dateEditText.setText(newText)
                        dateEditText.setSelection(dateEditText.text!!.length)
                    }
                } else if (currentCursorPosition == 3) {
                    val newText = inputText.dropLast(2)
                    dateEditText.setText(newText)
                    dateEditText.setSelection(dateEditText.text!!.length)
                }
            }
            false
        }
    }

    fun validateDate(dataString: String, char: Char): String {
        var diaValido: Int = 0
        var returnString = ""

        // Obtenha o dia do mês
        val diaDoMesAtual = calendar.get(Calendar.DAY_OF_MONTH)
        var mesAtual = calendar.get(Calendar.MONTH)
        mesAtual++
        val anoAtual = calendar.get(Calendar.YEAR)

        if (char == 'd') {
            val dia = dataString.toIntOrNull() ?: 1
            diaValido = if (dia > 31 || dia < 1) diaDoMesAtual else dia
            returnString = String.format("%02d/", diaValido)
        }
        if (char == 'm') {
            val partes = dataString.split("/")
            val dia = partes[0].toIntOrNull() ?: 1
            val mes = partes[1].toIntOrNull() ?: 1
            val mesValido = if (mes > 12 || mes < 1) mesAtual else mes
            returnString = String.format("%02d/%02d/", dia, mesValido)
        }
        if (char == 'a') {
            val partes = dataString.split("/")
            val dia = partes[0].toIntOrNull() ?: 1
            val mes = partes[1].toIntOrNull() ?: 1
            val ano = partes[2].toIntOrNull() ?: 2023
            val anoValido = if (ano < 2000 || ano > 2100) anoAtual else ano
            returnString = String.format("%02d/%02d/%04d", dia, mes, anoValido)
        }
        return returnString
    }

    private fun maskAmount() {
        amountInReal = AmountInReal(binding.etAmount)
        binding.etAmount.addTextChangedListener(amountInReal)
    }

    private fun showOrHideHint() {
        setupEditTextBehavior(binding.etAmount, binding.tilAmount, binding.tilAmount.hint)
        setupEditTextBehavior(
            binding.etDescription,
            binding.tilDescription,
            binding.tilDescription.hint
        )
        setupEditTextBehavior(binding.etDate, binding.tilDate, binding.tilDate.hint)
    }

    private fun setupEditTextBehavior(
        et: TextInputEditText,
        til: TextInputLayout,
        hint: CharSequence?
    ) {
        et.addTextChangedListener(onTextChanged = { editable, _, _, _ ->
            val newText = editable?.length ?: 0
            til.hint = if (newText > 0) "" else hint
        })
    }

    private fun showNavigation() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.showBottomNavigation() // Chamar o método para mostrar a BottomNavigationView
    }

    private fun hideNavigation() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.hideBottomNavigation() // Chamada para um método na MainActivity
    }

    private fun clickBtnBack() {
        binding.ibBackMovement.setOnClickListener {
            goToFragmentHome()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        showNavigation()
    }

}


