package com.yaojia.snowball.ui.kitchen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.yaojia.snowball.data.network.NetworkModule
import com.yaojia.snowball.databinding.FragmentKitchenBinding
import com.yaojia.snowball.ui.common.OrderAdapter
import kotlinx.coroutines.launch

class KitchenFragment : Fragment() {

    private var _binding: FragmentKitchenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val kitchenViewModel =
            ViewModelProvider(this).get(KitchenViewModel::class.java)

        _binding = FragmentKitchenBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = OrderAdapter { order ->
            // Handle action click
            val nextStatus = if (order.status == "PENDING") "PREPARING" else "READY"
            lifecycleScope.launch {
                try {
                    NetworkModule.apiService.updateOrderStatus(order.id, mapOf("status" to nextStatus))
                    kitchenViewModel.fetchOrders() // Refresh list
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.recyclerViewKitchen.adapter = adapter

        kitchenViewModel.orders.observe(viewLifecycleOwner) { orders ->
            adapter.submitList(orders)
        }

        kitchenViewModel.text.observe(viewLifecycleOwner) {
            if (it.startsWith("Error")) {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
