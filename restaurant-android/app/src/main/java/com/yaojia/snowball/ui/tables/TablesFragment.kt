package com.yaojia.snowball.ui.tables

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.yaojia.snowball.databinding.FragmentTablesBinding
import com.yaojia.snowball.ui.common.TableAdapter

class TablesFragment : Fragment() {

    private var _binding: FragmentTablesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val tablesViewModel =
            ViewModelProvider(this).get(TablesViewModel::class.java)

        _binding = FragmentTablesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = TableAdapter { table ->
            val dialog = BillDialogFragment.newInstance(table.id, table.tableNumber)
            dialog.show(parentFragmentManager, "BillDialog")
        }
        binding.recyclerViewTables.adapter = adapter

        tablesViewModel.tables.observe(viewLifecycleOwner) { tables ->
            adapter.submitList(tables)
        }

        tablesViewModel.text.observe(viewLifecycleOwner) {
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
