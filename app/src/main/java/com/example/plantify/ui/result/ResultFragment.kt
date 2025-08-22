package com.example.plantify.ui.result

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.plantify.R
import com.example.plantify.data.HistoryItem
import com.example.plantify.data.TreatmentRepository
import com.example.plantify.databinding.FragmentResultBinding
import com.example.plantify.ml.ClassificationResult
import com.example.plantify.ml.DiseaseClassifier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var classifier: DiseaseClassifier

    // Firebase variables
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classifier = DiseaseClassifier(requireContext())
        classifier.init()

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUriString = arguments?.getString("imageUri")
        if (imageUriString == null) {
            binding.diseaseNameResult.text = "No image provided"
            return
        }

        val imageUri = Uri.parse(imageUriString)
        startAnalysis(imageUri)
    }

    private fun startAnalysis(imageUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                requireActivity().contentResolver.openInputStream(imageUri).use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap == null) {
                        withContext(Dispatchers.Main) {
                            binding.diseaseNameResult.text = "Image could not be loaded"
                            Toast.makeText(requireContext(), "Failed to decode image", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    withContext(Dispatchers.Main) {
                        Glide.with(this@ResultFragment).load(bitmap).centerCrop().into(binding.resultImage)
                        binding.diseaseNameResult.text = "Classifying..."
                        binding.confidenceText.visibility = View.GONE
                    }

                    val result = classifier.classify(bitmap)

                    withContext(Dispatchers.Main) {
                        if (!isAdded) return@withContext
                        saveResultToDatabase(result) // on main thread
                        updateUiWithResult(result)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.diseaseNameResult.text = "Error processing image"
                    Toast.makeText(requireContext(), e.localizedMessage ?: "Unknown error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // In ResultFragment.kt

    private fun saveResultToDatabase(result: ClassificationResult) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        val historyRef = database.getReference("history").child(userId).push()

        val diseaseType = if (result.diseaseName.contains("Healthy", ignoreCase = true)) {
            "Healthy"
        } else {
            "Diseased"
        }

        val historyItem = HistoryItem(
            diseaseName = result.diseaseName.replace("_", " "),
            confidence = result.confidence,
            timestamp = System.currentTimeMillis(),
            type = diseaseType
        )

        historyRef.setValue(historyItem)
            .addOnSuccessListener {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Result saved successfully", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // ADD THIS LOG to see the specific error in Logcat
                android.util.Log.e("DatabaseSaveError", "Failed to save result", e)

                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to save result", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUiWithResult(result: ClassificationResult) {
        val formattedDiseaseName = result.diseaseName.replace("_", " ")
        binding.diseaseNameResult.text = formattedDiseaseName
        binding.confidenceText.text = "${(result.confidence * 100).toInt()}% Confident"
        binding.confidenceText.visibility = View.VISIBLE

        if (result.diseaseName.contains("Healthy", ignoreCase = true)) {
            binding.diseaseNameResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_primary))
            binding.pesticideTitle.visibility = View.GONE
            binding.pesticideText.visibility = View.GONE
            binding.guideTitle.text = "Care Guide"
            binding.guideText.text = "Your plant appears to be healthy. Keep up the great work with regular watering and proper sunlight."
        } else {
            binding.diseaseNameResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_error))
            binding.pesticideTitle.visibility = View.VISIBLE
            binding.pesticideText.visibility = View.VISIBLE
            binding.guideTitle.text = "Treatment Guide"
            val treatment = TreatmentRepository.getTreatment(result.diseaseName)
            binding.pesticideText.text = treatment.pesticide
            binding.guideText.text = treatment.guide
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't close classifier here — let it live for the app lifetime
        // classifier.close()
    }
}