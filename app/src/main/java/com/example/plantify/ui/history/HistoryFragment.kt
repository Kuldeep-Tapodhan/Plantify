package com.example.plantify.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plantify.data.Scan
import com.example.plantify.databinding.FragmentHistoryBinding
import com.example.plantify.ui.adapters.HistoryAdapter

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFullHistory()
    }

    private fun setupFullHistory() {
        val mockScans = listOf(
            Scan("1", "Apple Scab", "Aug 11, 2025", "https://placehold.co/100x100/e8117f/ffffff?text=Apple", false),
            Scan("2", "Healthy Cotton Plant", "Aug 09, 2025", "https://placehold.co/100x100/4CAF50/ffffff?text=Healthy", true),
            Scan("3", "Tomato Late Blight", "Aug 05, 2025", "https://placehold.co/100x100/f44336/ffffff?text=Tomato", false),
            Scan("4", "Grape Powdery Mildew", "Aug 02, 2025", "https://placehold.co/100x100/9c27b0/ffffff?text=Grape", false)
        )
        binding.historyRecyclerViewFull.layoutManager = LinearLayoutManager(context)
        binding.historyRecyclerViewFull.adapter = HistoryAdapter(mockScans)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
