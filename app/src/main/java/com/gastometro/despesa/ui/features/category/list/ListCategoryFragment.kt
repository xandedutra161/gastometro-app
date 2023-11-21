package com.gastometro.despesa.ui.features.category.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gastometro.despesa.R
import com.gastometro.despesa.databinding.FragmentListCategoryBinding
import com.gastometro.despesa.ui.adapters.CategoryAdapter
import com.gastometro.despesa.ui.base.BaseFragment
import com.gastometro.despesa.ui.state.ResourceState
import com.gastometro.despesa.util.hide
import com.gastometro.despesa.util.show
import com.gastometro.despesa.util.toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListCategoryFragment : BaseFragment<FragmentListCategoryBinding, ListCategoryViewModel>() {
    override val viewModel: ListCategoryViewModel by viewModels()
    private val categoryAdapter by lazy { CategoryAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycleView()
        clickFab()
        clickAdapter()
        clickPopupMenu()
        observer()
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListCategoryBinding =
        FragmentListCategoryBinding.inflate(inflater, container, false)

    private fun setupRecycleView() = with(binding) {
        rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context)
        }
        ItemTouchHelper(itemTouchHelperCallback()).attachToRecyclerView(rvCategories)
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
                val category = categoryAdapter.getCategoryPosition(viewHolder.adapterPosition)
                viewModel.delete(category).also {
                    toast(getString(R.string.message_delete_category))
                }
            }
        }
    }

    private fun clickPopupMenu() {
        categoryAdapter.setOnMenuItemClickListener { result ->
            if (result.second!!.isNotEmpty()) {
                val btnType = result.second
                if (btnType == "edit") {
                    val c = result.first
                    val action = ListCategoryFragmentDirections
                        .actionListCategoryFragmentToRegisterCategoryFragment(c)
                    findNavController().navigate(action)
                }
                if(btnType == "del") {
                    val c = result.first
                    viewModel.delete(c!!).also {
                        toast(getString(R.string.message_delete_category))
                    }
                }
            }
        }
    }


    private fun clickAdapter() {
        categoryAdapter.setOnClickListener { category ->
            val action = ListCategoryFragmentDirections
                .actionListCategoryFragmentToRegisterCategoryFragment(category)
            findNavController().navigate(action)
        }
    }

    private fun observer() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categories.collect { resource ->
                when (resource) {
                    is ResourceState.Success -> {
                        resource.data?.let {
                            binding.tvEmptyList.hide()
                            categoryAdapter.categories = it.toList()
                        }
                    }

                    is ResourceState.Error -> {
                        toast("teste deu algum erro")
                    }

                    is ResourceState.Empty -> {
                        binding.tvEmptyList.show()
                    }

                    else -> {
                    }
                }
            }
        }
    }

    private fun clickFab() {
        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)

        fab.setOnClickListener {
            val action =
                ListCategoryFragmentDirections.actionListCategoryFragmentToRegisterCategoryFragment()
            findNavController().navigate(action)
        }
    }


}