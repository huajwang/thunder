package com.yaojia.snowball.ui.staff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.yaojia.snowball.data.network.NetworkModule
import com.yaojia.snowball.databinding.FragmentStaffBinding
import com.yaojia.snowball.ui.common.OrderAdapter
import kotlinx.coroutines.launch

class StaffFragment : Fragment() {

    private var _binding: FragmentStaffBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val staffViewModel =
            ViewModelProvider(this).get(StaffViewModel::class.java)

        _binding = FragmentStaffBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = OrderAdapter { order ->
            // Handle action click (Serve)
            lifecycleScope.launch {
                try {
                    NetworkModule.apiService.updateOrderStatus(order.id, mapOf("status" to "COMPLETED"))
                    staffViewModel.fetchReadyOrders() // Refresh list
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.recyclerViewStaff.adapter = adapter

        staffViewModel.readyOrders.observe(viewLifecycleOwner) { orders ->
            adapter.submitList(orders)
        }

        staffViewModel.text.observe(viewLifecycleOwner) {
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
