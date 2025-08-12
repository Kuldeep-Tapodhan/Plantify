package com.example.plantify.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plantify.R
import com.example.plantify.data.Scan
import com.example.plantify.databinding.FragmentHomeBinding
import com.example.plantify.ui.adapters.HistoryAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecentScans()

        binding.scanButton.setOnClickListener {
            // In a real app, this would launch the camera/gallery
            // For now, we navigate to the result screen
            findNavController().navigate(R.id.navigation_result)
        }
    }

    private fun setupRecentScans() {
        val mockScans = listOf(
            Scan("1", "Apple Scab", "Today, 11:45 AM", "https://placehold.co/100x100/e8117f/ffffff?text=Apple", false),
            Scan("2", "Healthy Cotton Plant", "Aug 09, 2025", "https://placehold.co/100x100/4CAF50/ffffff?text=Healthy", true)
        )
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.historyRecyclerView.adapter = HistoryAdapter(mockScans)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
