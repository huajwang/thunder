package com.yaojia.snowball.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yaojia.snowball.data.model.Order
import com.yaojia.snowball.databinding.ItemOrderBinding

class OrderAdapter(
    private val onActionClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding, onActionClick)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderViewHolder(
        private val binding: ItemOrderBinding,
        private val onActionClick: (Order) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.textTableNumber.text = "Table #${order.tableId}"
            binding.textOrderStatus.text = order.status
            
            val itemsSummary = order.items.joinToString("\n") { "${it.quantity}x ${it.menuItemName}" }
            binding.textOrderItems.text = itemsSummary

            binding.buttonAction.text = when (order.status) {
                "PENDING" -> "Start Preparing"
                "PREPARING" -> "Mark Ready"
                "READY" -> "Serve"
                else -> "Action"
            }

            binding.buttonAction.setOnClickListener {
                onActionClick(order)
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
