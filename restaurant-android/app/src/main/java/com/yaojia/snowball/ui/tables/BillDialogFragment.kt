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

        val adapter = BillAdapter()
        binding.recyclerViewBillOrders.adapter = adapter

        binding.buttonClose.setOnClickListener {
            dismiss()
        }

        lifecycleScope.launch {
            loadBill(tableId, adapter)
            
            val restaurantId = NetworkModule.getTokenManager().getRestaurantId()
            NetworkModule.realtimeService.subscribeToOrders(restaurantId).collect {
                loadBill(tableId, adapter)
            }
        }
    }

    private suspend fun loadBill(tableId: Long, adapter: BillAdapter) {
        try {
            val orders = NetworkModule.apiService.getTableBill(tableId)
            adapter.submitList(orders)
            
            val subTotal = orders.sumOf { it.subTotal }
            val tax = orders.sumOf { it.tax }
            val discount = orders.sumOf { it.discount }
            val total = orders.sumOf { it.totalAmount }
            
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
