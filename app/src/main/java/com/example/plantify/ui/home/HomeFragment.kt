package com.example.plantify.ui.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plantify.R
import com.example.plantify.data.Scan
import com.example.plantify.databinding.FragmentHomeBinding
import com.example.plantify.ui.adapters.HistoryAdapter
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var latestTmpUri: Uri? = null

    // Activity Result Launcher for Camera
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            latestTmpUri?.let { uri ->
                val bundle = bundleOf("imageUri" to uri.toString())
                findNavController().navigate(R.id.navigation_result, bundle)
            }
        }
    }

    // Activity Result Launcher for Gallery
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                val bundle = bundleOf("imageUri" to it.toString())
                findNavController().navigate(R.id.navigation_result, bundle)
            }
        }
    }

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
            showImageSelectionDialog()
        }
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Use Camera", "Upload from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun openCamera() {
        val tmpFile = File.createTempFile("temp_image", ".jpg", requireContext().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        latestTmpUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            tmpFile
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, latestTmpUri)
        }
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun setupRecentScans() {
        val mockScans = listOf(
            Scan("1", "Apple Scab", "Today, 03:21 PM", "https://placehold.co/100x100/e8117f/ffffff?text=Apple", false),
            Scan("2", "Healthy Cotton Plant", "Aug 11, 2025", "https://placehold.co/100x100/4CAF50/ffffff?text=Healthy", true)
        )
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.historyRecyclerView.adapter = HistoryAdapter(mockScans)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}