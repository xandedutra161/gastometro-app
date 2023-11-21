package com.gastometro.despesa.ui.features.movement.register

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gastometro.despesa.databinding.BottomSheetCategoryBinding
import com.gastometro.despesa.ui.adapters.BottomSheetCategoriesAdapter
import com.gastometro.despesa.ui.base.BaseBottomSheetFragment
import com.gastometro.despesa.ui.state.ResourceState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetCategoriesFragment @Inject constructor(
    private val viewModel: RegisterMovementViewModel
) : BaseBottomSheetFragment<BottomSheetCategoryBinding>() {
    private val bottomSheetCategoriesAdapter by lazy { BottomSheetCategoriesAdapter() }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): BottomSheetCategoryBinding =
        BottomSheetCategoryBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupModalBottomSheet(view)
        setupRecyclerView()
        clickAdapter()
        searchInit()
        observer()
    }

    private fun setupModalBottomSheet(view: View) {
        // Aqui estamos configurando o BottomSheetBehavior do fragmento para STATE_EXPANDED
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // Deixando o recyclerview do tamanho da tela e setando a altura do bottom sheet quando é recolhida. (peekHeight)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        val recyclerView = binding.rvSelectCategories
        val params = recyclerView.layoutParams
        params.height = screenHeight
        behavior.peekHeight = screenHeight

        recyclerView.layoutParams = params

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> binding.etSearchCategory.setText("")
                    else -> false
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // distancia que foi deslizado o bottom sheet na tela(slideOffset)
            }

        })

    }

    private fun searchInit() = with(binding) {
        etSearchCategory.addTextChangedListener(onTextChanged = { editable, _, _, _ ->
            val newText = editable?.length ?: 0
            if (newText > 0) searchCategory() else viewModel.getAllCategories()
        })
    }


    private fun searchCategory() = with(binding) {
        Log.i("teste", "searchCategory")
        etSearchCategory.editableText.trim().let {
            if (it.isNotEmpty()) {
                searchQuery(it.toString())
            }
        }
    }

    private fun searchQuery(query: String) {
        viewModel.searchCategoryName(query)
    }

    private fun clickAdapter() {
        bottomSheetCategoriesAdapter.setOnClickListener { category ->
            viewModel.setSelectedCategory(category.name)
            binding.etSearchCategory.setText("")
            dismiss()
        }
    }

    private fun setupRecyclerView() = with(binding) {
        rvSelectCategories.apply {
            adapter = bottomSheetCategoriesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observer() {

        lifecycleScope.launch {
            viewModel.categories.collect { resource ->
                when (resource) {
                    is ResourceState.Success -> {
                        resource.data?.let {
                            bottomSheetCategoriesAdapter.categories = it.toList()
                        }
                    }

                    is ResourceState.Error -> {
                        //toast("teste deu algum erro")
                    }

                    is ResourceState.Empty -> {
                        //toast("teste deu algum erro")
                    }

                    else -> {
                    }
                }
            }
        }
    }


}