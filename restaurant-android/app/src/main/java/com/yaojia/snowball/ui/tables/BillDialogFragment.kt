package com.yaojia.snowball.ui.tables

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yaojia.snowball.data.network.NetworkModule
import com.yaojia.snowball.databinding.DialogBillBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogBillBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val ARG_TABLE_ID = "table_id"
        const val ARG_TABLE_NUMBER = "table_number"

        fun newInstance(tableId: Long, tableNumber: Int): BillDialogFragment {
            val fragment = BillDialogFragment()
            val args = Bundle()
            args.putLong(ARG_TABLE_ID, tableId)
            args.putInt(ARG_TABLE_NUMBER, tableNumber)
            fragment.arguments = args
            return fragment
        }
    }

    private var currentOrders: List<com.yaojia.snowball.data.model.Order> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        dialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        dialog?.behavior?.skipCollapsed = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tableId = arguments?.getLong(ARG_TABLE_ID) ?: return
        val tableNumber = arguments?.getInt(ARG_TABLE_NUMBER) ?: 0

        binding.textBillTitle.text = "Bill for Table #$tableNumber"
        
        // Set current date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        binding.textBillDate.text = "Date: ${dateFormat.format(Date())}"

        val adapter = BillAdapter()
        binding.recyclerViewBillOrders.adapter = adapter

        binding.buttonClose.setOnClickListener {
            dismiss()
        }

        binding.buttonPrintReceipt.setOnClickListener {
            Toast.makeText(context, "Print Receipt clicked", Toast.LENGTH_SHORT).show()
        }

        binding.buttonPaid.setOnClickListener {
            lifecycleScope.launch {
                try {
                    currentOrders.forEach { order ->
                        NetworkModule.apiService.updateOrderStatus(order.id, mapOf("status" to "PAID"))
                    }
                    Toast.makeText(context, "Orders marked as PAID", Toast.LENGTH_SHORT).show()
                    dismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error updating orders: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        lifecycleScope.launch {
            val restaurantId = NetworkModule.getTokenManager().getRestaurantId()
            loadRestaurantInfo(restaurantId)
            loadBill(tableId, adapter)
            
            NetworkModule.realtimeService.subscribeToOrders(restaurantId).collect {
                loadBill(tableId, adapter)
            }
        }
    }

    private suspend fun loadRestaurantInfo(restaurantId: Long) {
        try {
            val restaurant = NetworkModule.apiService.getRestaurant(restaurantId)
            binding.textRestaurantName.text = restaurant.name
        } catch (e: Exception) {
            binding.textRestaurantName.text = "Restaurant"
        }
    }

    private suspend fun loadBill(tableId: Long, adapter: BillAdapter) {
        try {
            val allOrders = NetworkModule.apiService.getTableBill(tableId)
            currentOrders = allOrders.filter { it.status != "PAID" && it.status != "CANCELLED" }
            adapter.submitList(currentOrders)
            
            val subTotal = currentOrders.sumOf { it.subTotal }
            val tax = currentOrders.sumOf { it.tax }
            val discount = currentOrders.sumOf { it.discount }
            val total = currentOrders.sumOf { it.totalAmount }
            
            binding.textSubtotal.text = "Subtotal: $%.2f".format(subTotal)
            binding.textTax.text = "Tax: $%.2f".format(tax)
            binding.textDiscount.text = "Discount: -$%.2f".format(discount)
            binding.textTotalAmount.text = "Total: $%.2f".format(total)
        } catch (e: Exception) {
            // Toast.makeText(context, "Error fetching bill: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
