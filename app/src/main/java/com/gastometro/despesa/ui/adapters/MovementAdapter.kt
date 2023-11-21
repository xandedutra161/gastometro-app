package com.gastometro.despesa.ui.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gastometro.despesa.R
import com.gastometro.despesa.data.model.Movement
import com.gastometro.despesa.databinding.ItemMovementBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

class MovementAdapter : RecyclerView.Adapter<MovementAdapter.MovementViewHolder>() {

    inner class MovementViewHolder(val binding: ItemMovementBinding) :
        RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Movement>() {

        override fun areItemsTheSame(oldItem: Movement, newItem: Movement): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: Movement, newItem: Movement): Boolean {
            return oldItem.movementId == newItem.movementId && oldItem.amount == newItem.amount &&
                    oldItem.paid == newItem.paid && oldItem.categoryName == newItem.categoryName &&
                    oldItem.description == newItem.description && oldItem.date == newItem.date
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    var movements: List<Movement>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovementViewHolder {
        return MovementViewHolder(
            ItemMovementBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = movements.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MovementViewHolder, position: Int) {

        val movement = movements[position]
        holder.binding.apply {
            val amount = movement.amount
            val formatNumber = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            val amountFormated = formatNumber.format(amount)


            if (movement.paid) {
                tvAmount.setTextColor(ContextCompat.getColor(root.context.applicationContext, R.color.green)) // amarelo
            }
            if (!movement.paid) {
                if (movement.date.isEqual(LocalDate.now()) || movement.date.isAfter(LocalDate.now())){
                    tvAmount.setTextColor(ContextCompat.getColor(root.context.applicationContext, R.color.yellow)) // amarelo
                } else{
                    tvAmount.setTextColor(ContextCompat.getColor(root.context.applicationContext, R.color.red)) // amarelo
                }
            }

            var formatParcels = ""
            if (movement.numberInstallments > 1 && !movement.fixed) {
                formatParcels = "${movement.currentInstallments}/${movement.numberInstallments}"
            }

            tvCountParcel.text = formatParcels
            tvDescription.text = movement.description
            tvCategoryNameItem.text = movement.categoryName
            tvDateMovement.text = formateDate(movement.date.toString())
            tvAmount.text = amountFormated
        }
        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                it(movement)
            }
        }

        holder.binding.popupMenuMovement.setOnClickListener {
            val popupMenu: ImageButton = holder.binding.popupMenuMovement

            showPopupMenu(popupMenu, movement)
        }
    }

    private fun showPopupMenu(view: View, movement: Movement) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.popup_menu) // Cria um arquivo XML em res/menu com as opções do menu
        popupMenu.setForceShowIcon(true)
        var btnName = ""

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.editText -> {
                    btnName = "edit"
                    val returnPopupMenu = Pair(movement, btnName)
                    onPopupMenuClickListener?.let { it ->
                        it(returnPopupMenu)
                    }
                    true
                }

                R.id.delete -> {
                    btnName = "del"
                    val returnPopupMenu = Pair(movement, btnName)
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

    private var onPopupMenuClickListener: ((Pair<Movement?, String?>) -> Unit)? = null

    fun setOnMenuItemClickListener(listener: (Pair<Movement?, String?>) -> Unit) {
        onPopupMenuClickListener = listener
    }

    private var onItemClickListener: ((Movement) -> Unit)? = null

    fun setOnClickListener(listener: (Movement) -> Unit) {
        onItemClickListener = listener
    }

    fun getMovementPosition(position: Int): Movement {
        return movements[position]
    }

    private fun formateDate(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy")
        return outputFormat.format(inputFormat.parse(date))
    }

}