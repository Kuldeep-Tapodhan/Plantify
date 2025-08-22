package com.example.plantify.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plantify.data.HistoryItem
import com.example.plantify.databinding.FragmentHistoryBinding
import com.example.plantify.ui.adapters.HistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the RecyclerView with a default empty adapter
        binding.historyRecyclerViewFull.layoutManager = LinearLayoutManager(context)
        binding.historyRecyclerViewFull.adapter = HistoryAdapter(emptyList()) // Start with an empty list

        // Fetch the data from Firebase
        setupFirebaseHistoryListener()
    }

    private fun setupFirebaseHistoryListener() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.w("HistoryFragment", "User is not logged in.")
            return
        }

        val historyRef = database.getReference("history").child(userId)

        historyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val historyList = mutableListOf<HistoryItem>()
                if (snapshot.exists()) {
                    for (historySnapshot in snapshot.children) {
                        val historyItem = historySnapshot.getValue(HistoryItem::class.java)
                        historyItem?.let { historyList.add(it) }
                    }
                    // Sort the list to show the most recent scans first
                    historyList.sortByDescending { it.timestamp }
                }
                // Update the RecyclerView with the new data
                binding.historyRecyclerViewFull.adapter = HistoryAdapter(historyList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistoryFragment", "Failed to read history.", error.toException())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}