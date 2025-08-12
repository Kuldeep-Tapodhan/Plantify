package com.example.plantify.ui.result

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.plantify.databinding.FragmentResultBinding

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the image URI from the arguments
        val imageUriString = arguments?.getString("imageUri")

        val imageToLoad = if (imageUriString != null) {
            Uri.parse(imageUriString)
        } else {
            // Load a placeholder if no URI is passed (e.g., from camera bitmap)
            "https://placehold.co/400x300/f44336/ffffff?text=Diseased+Leaf"
        }

        Glide.with(this)
            .load(imageToLoad)
            .into(binding.resultImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}