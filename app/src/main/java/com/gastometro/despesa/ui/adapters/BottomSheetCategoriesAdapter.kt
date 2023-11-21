package com.gastometro.despesa.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gastometro.despesa.data.model.Category
import com.gastometro.despesa.databinding.ItemSelectCategoryBinding

class BottomSheetCategoriesAdapter : RecyclerView.Adapter<BottomSheetCategoriesAdapter.BottomSheetCategoriesViewHolder>() {

    inner class BottomSheetCategoriesViewHolder(val binding: ItemSelectCategoryBinding) :
            RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Category>() {

        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.categoryId == newItem.categoryId && oldItem.name == newItem.name
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    var categories: List<Category>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BottomSheetCategoriesViewHolder {
        return BottomSheetCategoriesViewHolder(
            ItemSelectCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: BottomSheetCategoriesViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.apply {
            tvSelectCategory.text = category.name
        }
        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(category)
            }
        }
    }

    private var onItemClickListener: ((Category) -> Unit)? = null
    fun setOnClickListener(listener: (Category) -> Unit) {
        onItemClickListener = listener
    }


}