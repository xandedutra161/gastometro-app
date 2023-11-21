package com.gastometro.despesa.ui.features.category.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gastometro.despesa.R
import com.gastometro.despesa.data.model.Category
import com.gastometro.despesa.databinding.FragmentRegisterCategoryBinding
import com.gastometro.despesa.ui.activities.MainActivity
import com.gastometro.despesa.ui.base.BaseFragment
import com.gastometro.despesa.util.toast
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterCategoryFragment :
    BaseFragment<FragmentRegisterCategoryBinding, RegisterCategoryViewModel>() {

    override val viewModel: RegisterCategoryViewModel by viewModels()
    private val args: RegisterCategoryFragmentArgs by navArgs()
    private var updateCategory = false

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRegisterCategoryBinding =
        FragmentRegisterCategoryBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val argsName = args.category?.name

        argsName?.let {
            binding.btnSave.text = getString(R.string.changeBtnSave)
            binding.etCategory.setText(argsName)
            updateCategory = true
        }
        hideNavigation()
        cleanHelper()
        clickBtnSave()
        clickBtnBack(view)
    }

    private fun cleanHelper() = with(binding){
        clearHelperText(etCategory, tilCategory)
    }

    private fun clearHelperText(editText: EditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener { s ->
            textInputLayout.helperText = if (s.isNullOrEmpty()) null else ""
        }
    }

    private fun clickBtnSave() {
        binding.btnSave.setOnClickListener {

            val nameCategory = binding.etCategory.text.toString()

            if(!validateField(nameCategory)) {
                return@setOnClickListener
            }


            viewLifecycleOwner.lifecycleScope.launch {

                if (viewModel.isCategoryExists(nameCategory)) {
                    toast(getString(R.string.message_duplicate_category))
                    return@launch
                }

                if (updateCategory) {
                    val update = Category(categoryId = args.category!!.categoryId, name = nameCategory)
                    viewModel.update(update)
                    toast(getString(R.string.message_update_category))

                    showNavigation()
                    findNavController().navigate(R.id.listCategoryFragment)
                } else {
                    val newCategory = Category(name = nameCategory)
                    viewModel.insert(newCategory)
                    toast(getString(R.string.message_register_category))

                    showNavigation()
                    findNavController().navigate(R.id.listCategoryFragment)
                }
            }

        }
    }

    private fun validateField(nameCategory: String): Boolean {
        binding.tilCategory.helperText = ""

        var result = true
        if (nameCategory.isEmpty()) {
            binding.tilCategory.helperText = "Informar Categoria."
            result = false
        }

        return result

    }

    private fun showNavigation() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.showBottomNavigation() // Chamar o método para mostrar a BottomNavigationView
    }

    private fun hideNavigation() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.hideBottomNavigation() // Chamada para um método na MainActivity
    }

    private fun clickBtnBack(view: View) {
        binding.ibBack.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        showNavigation()
    }

}