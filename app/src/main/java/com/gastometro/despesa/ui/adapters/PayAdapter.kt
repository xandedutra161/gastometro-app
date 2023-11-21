package com.gastometro.despesa.ui.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gastometro.despesa.R
import com.gastometro.despesa.data.model.Movement
import com.gastometro.despesa.databinding.ItemPayBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

class PayAdapter : RecyclerView.Adapter<PayAdapter.PayViewHolder>() {
    val markedItems = mutableListOf<Movement>()

    inner class PayViewHolder(val binding: ItemPayBinding) : RecyclerView.ViewHolder(binding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Movement>() {

        override fun areItemsTheSame(oldItem: Movement, newItem: Movement): Boolean {
            return oldItem.movementId == newItem.movementId
        }

        override fun areContentsTheSame(oldItem: Movement, newItem: Movement): Boolean {
            return return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    var movements: List<Movement>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayViewHolder {
        return PayViewHolder(
            ItemPayBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }


    override fun getItemCount(): Int = movements.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PayViewHolder, position: Int) {
        val movement = movements[position]
        holder.binding.apply {
            val amount = movement.amount
            val formatNumber = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            val amountFormated = formatNumber.format(amount)

            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    onItemClickListener?.let {
                        it(movement)
                    }
                }

                if (checkBox.isChecked) markedItems.add(movement) else markedItems.remove(movement)
            }

            var formatParcels = ""
            if (movement.numberInstallments > 1 && !movement.fixed) {
                formatParcels = "${movement.currentInstallments}/${movement.numberInstallments}"
            }

            tvParcelCount.text = formatParcels
            tvDescription.text = movement.description
            tvCategoryNameItem.text = movement.categoryName
            tvDateExpiration.text = formateDate(movement.date.toString())

            if (movement.paid) {

                tvAmount.text = "$amountFormated"
                tvAmount.setTextColor(root.resources.getColor(R.color.green))
                checkBox.isEnabled = false
                checkBox.isChecked = true
                itemPay.setBackgroundColor(root.resources.getColor(R.color.background_paid))
            }

            if (!movement.paid) {
                tvAmount.text = "$amountFormated"
                checkBox.isChecked = false
                checkBox.isEnabled = true
                if (movement.date.isEqual(LocalDate.now()) || movement.date.isAfter(LocalDate.now())) {
                    tvAmount.setTextColor(
                        ContextCompat.getColor(
                            root.context.applicationContext,
                            R.color.yellow
                        )
                    ) // amarelo
                } else {
                    tvAmount.setTextColor(
                        ContextCompat.getColor(
                            root.context.applicationContext,
                            R.color.red
                        )
                    ) // amarelo
                }
                itemPay.setBackgroundColor(root.resources.getColor(R.color.backgroundColor))
            }
        }
    }

    private var onItemClickListener: ((Movement) -> Unit)? = null

    fun setOnClickListener(listener: (Movement) -> Unit) {
        onItemClickListener = listener
    }

    private fun formateDate(date: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy")
        return outputFormat.format(inputFormat.parse(date))
    }


}

