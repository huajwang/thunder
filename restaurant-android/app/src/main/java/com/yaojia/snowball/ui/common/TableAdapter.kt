package com.yaojia.snowball.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yaojia.snowball.data.model.Table
import com.yaojia.snowball.databinding.ItemTableBinding

class TableAdapter(
    private val onTableClick: (Table) -> Unit
) : ListAdapter<Table, TableAdapter.TableViewHolder>(TableDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val binding = ItemTableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TableViewHolder(binding, onTableClick)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TableViewHolder(
        private val binding: ItemTableBinding,
        private val onTableClick: (Table) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(table: Table) {
            binding.textTableNumber.text = "Table ${table.tableNumber}"
            binding.root.setOnClickListener {
                onTableClick(table)
            }
        }
    }

    class TableDiffCallback : DiffUtil.ItemCallback<Table>() {
        override fun areItemsTheSame(oldItem: Table, newItem: Table): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Table, newItem: Table): Boolean {
            return oldItem == newItem
        }
    }
}
