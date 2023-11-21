package com.gastometro.despesa.ui.adapters

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gastometro.despesa.R
import com.gastometro.despesa.data.model.Category
import com.gastometro.despesa.databinding.ItemCategoryBinding

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.apply {
            tvCategoryName.text = category.name
        }
        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(category)
            }
        }

        holder.binding.popupMenuCategory.setOnClickListener {
            val popupMenu: ImageButton = holder.binding.popupMenuCategory
            
            showPopupMenu(popupMenu, category)
        }
    }

    private fun showPopupMenu(view: View, category: Category) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.popup_menu) // Cria um arquivo XML em res/menu com as opções do menu
        popupMenu.setForceShowIcon(true)
        var btnName = ""

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.editText -> {
                    btnName = "edit"
                    val returnPopupMenu = Pair(category, btnName)
                    onPopupMenuClickListener?.let { it ->
                        it(returnPopupMenu)
                    }
                    true
                }

                R.id.delete -> {
                    btnName = "del"
                    val returnPopupMenu = Pair(category, btnName)
                    onPopupMenuClickListener?.let { it ->
                        it(returnPopupMenu)
                    }
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private var onItemClickListener: ((Category) -> Unit)? = null

    private var onPopupMenuClickListener: ((Pair<Category?, String?>) -> Unit)? = null

    fun setOnMenuItemClickListener(listener: (Pair<Category?, String?>) -> Unit) {
        onPopupMenuClickListener = listener
    }

    fun setOnClickListener(listener: (Category) -> Unit) {
        onItemClickListener = listener
    }

    fun getCategoryPosition(position: Int): Category {
        return categories[position]
    }

}