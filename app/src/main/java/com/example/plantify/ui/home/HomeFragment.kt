package com.example.plantify.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plantify.R
import com.example.plantify.data.HistoryItem
import com.example.plantify.data.WeatherResponse
import com.example.plantify.data.network.RetrofitInstance
import com.example.plantify.databinding.FragmentHomeBinding
import com.example.plantify.ml.DiseaseClassifier
import com.example.plantify.ui.adapters.HistoryAdapter
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var latestTmpUri: Uri? = null

    // Firebase & ML
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var classifier: DiseaseClassifier

    // Weather API Key
    private val WEATHER_API_KEY = "150c348bb4e2fa1d250361c6b272aae6"

    // Listeners
    private var historyListener: ValueEventListener? = null

    // Permission launchers
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required.", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getCurrentLocationAndFetchWeather()
            } else {
                Toast.makeText(requireContext(), "Location permission is required for weather updates.", Toast.LENGTH_LONG).show()
            }
        }

    // Activity result launchers
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            latestTmpUri?.let { uri -> handleImageResult(uri) }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri -> handleImageResult(uri) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        classifier = DiseaseClassifier(requireContext())
        classifier.init()
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

        loadUserData()
        setupRecentScansListener()
        checkLocationPermissionAndFetchWeather()

        binding.scanButton.setOnClickListener {
            showImageSelectionDialog()
        }
    }

    /** ---------------- Weather Section ---------------- **/
    private fun checkLocationPermissionAndFetchWeather() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocationAndFetchWeather()
            }
            else -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationAndFetchWeather() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    fetchWeather(location.latitude, location.longitude)
                } else {
                    Toast.makeText(requireContext(), "Could not retrieve location.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getCurrentWeather(lat, lon, WEATHER_API_KEY)
                if (response.isSuccessful) {
                    response.body()?.let { weatherData ->
                        updateWeatherUI(weatherData)
                    }
                } else {
                    Log.e("WeatherFetch", "API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("WeatherFetch", "Network Error: ${e.message}")
            }
        }
    }

    private fun updateWeatherUI(weatherData: WeatherResponse) {
        if (_binding == null) return
        binding.locationText.text = weatherData.name
        binding.tempText.text = "${weatherData.main.temp.toInt()}°C"
        if (weatherData.weather.isNotEmpty()) {
            binding.weatherDescriptionText.text =
                weatherData.weather[0].description.replaceFirstChar { it.uppercase() }
        }
    }

    /** ---------------- Firebase User & History ---------------- **/
    private fun loadUserData() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            binding.userNameText.text = "Guest"
            return
        }
        val userRef = database.getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    binding.userNameText.text = name ?: "User"
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("HomeFragment", "Failed to load user name.", error.toException())
            }
        })
    }

    private fun setupRecentScansListener() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.historyRecyclerView.adapter = HistoryAdapter(emptyList())

        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            binding.recentScansTitle.visibility = View.GONE
            binding.historyRecyclerView.visibility = View.GONE
            return
        }

        val historyQuery = database.getReference("history").child(userId)
            .orderByChild("timestamp")
            .limitToLast(3)

        historyListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                val recentScans = mutableListOf<HistoryItem>()
                if (snapshot.exists()) {
                    for (scanSnapshot in snapshot.children) {
                        scanSnapshot.getValue(HistoryItem::class.java)?.let { recentScans.add(it) }
                    }
                    recentScans.reverse()
                }
                binding.historyRecyclerView.adapter = HistoryAdapter(recentScans)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeFragment", "Failed to load recent scans.", error.toException())
            }
        }

        historyQuery.addValueEventListener(historyListener!!)
    }

    /** ---------------- Camera & Gallery ---------------- **/
    private fun showImageSelectionDialog() {
        val options = arrayOf("Use Camera", "Upload from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGallery()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
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

    private fun handleImageResult(uri: Uri) {
        val bundle = bundleOf("imageUri" to uri.toString())
        findNavController().navigate(R.id.navigation_result, bundle)
    }

    /** ---------------- Lifecycle ---------------- **/
    override fun onDestroyView() {
        super.onDestroyView()
        historyListener?.let {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                database.getReference("history").child(userId).removeEventListener(it)
            }
        }
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}
