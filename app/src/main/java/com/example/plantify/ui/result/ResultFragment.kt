package com.example.plantify.ui.result

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.plantify.R
import com.example.plantify.data.TreatmentRepository
import com.example.plantify.databinding.FragmentResultBinding
import com.example.plantify.ml.ClassificationResult
import com.example.plantify.ml.DiseaseClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private lateinit var classifier: DiseaseClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the classifier
        classifier = DiseaseClassifier(requireContext())
        classifier.init()
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

        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)

            try {
                // Convert URI to Bitmap
                val inputStream = requireActivity().contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                if (bitmap != null) {
                    // Load the bitmap into the ImageView first
                    Glide.with(this)
                        .load(bitmap)
                        .centerCrop()
                        .into(binding.resultImage)

                    // Show a loading message
                    binding.diseaseNameResult.text = "Classifying..."
                    binding.confidenceText.visibility = View.GONE // Hide confidence while classifying

                    // Run classification in the background to avoid freezing the UI
                    lifecycleScope.launch(Dispatchers.Default) {
                        val result = classifier.classify(bitmap)

                        // Update the UI on the main thread with the result
                        withContext(Dispatchers.Main) {
                            updateUiWithResult(result)
                        }
                    }
                } else {
                    binding.diseaseNameResult.text = "Could not decode image"
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                binding.diseaseNameResult.text = "File not found"
            }
        }
    }

    private fun updateUiWithResult(result: ClassificationResult) {
        // Format the disease name by removing underscores
        val formattedDiseaseName = result.diseaseName.replace("_", " ")
        binding.diseaseNameResult.text = formattedDiseaseName

        // --- THIS IS THE KEY CHANGE ---
        // Set the confidence text and make it visible
        binding.confidenceText.text = "${(result.confidence * 100).toInt()}% Confident"
        binding.confidenceText.visibility = View.VISIBLE

        if (result.diseaseName.contains("Healthy", ignoreCase = true)) {
            // Handle the "Healthy" case
            binding.diseaseNameResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_primary))
            binding.pesticideTitle.visibility = View.GONE
            binding.pesticideText.visibility = View.GONE
            binding.guideTitle.text = "Care Guide"
            binding.guideText.text = "Your plant appears to be healthy. Keep up the great work with regular watering and proper sunlight."
        } else {
            // Handle the "Diseased" case
            binding.diseaseNameResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_error))
            binding.pesticideTitle.visibility = View.VISIBLE
            binding.pesticideText.visibility = View.VISIBLE
            binding.guideTitle.text = "Treatment Guide"

            // Look up and display the treatment information
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
        // Release the model resources
        classifier.close()
    }
}