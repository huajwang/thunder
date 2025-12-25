package com.yaojia.snowball.ui.tables

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yaojia.snowball.data.model.Order
import com.yaojia.snowball.databinding.ItemBillOrderBinding
import java.text.SimpleDateFormat
import java.util.Locale

class BillAdapter : ListAdapter<Order, BillAdapter.BillViewHolder>(BillDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = ItemBillOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BillViewHolder(private val binding: ItemBillOrderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            // Format time
            try {
                // Assuming createdAt is ISO string, but for simplicity just showing raw or parsing if standard
                // If createdAt is "2023-10-27T10:00:00Z", we might want to format it.
                // For now, let's just show the order ID or simple text
                binding.textOrderTime.text = "Order #${order.id}"
            } catch (e: Exception) {
                binding.textOrderTime.text = "Order #${order.id}"
            }

            val sb = StringBuilder()
            for (item in order.items) {
                val name = if (item.variantName != null) {
                    "${item.menuItemName} (${item.variantName})"
                } else {
                    item.menuItemName
                }
                
                // Truncate name if too long to fit nicely with price
                val displayName = if (name.length > 20) name.take(18) + ".." else name.padEnd(20)
                
                // Assuming item.price is unit price. 
                // If item.price is total for the line, then just use it.
                // Let's assume it is unit price for now.
                val lineTotal = item.price * item.quantity
                
                val line = String.format(Locale.US, "%dx %-20s $%.2f", item.quantity, displayName, lineTotal)
                sb.append(line).append("\n")
            }
            binding.textBillItems.text = sb.toString().trim()
        }
    }

    class BillDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
