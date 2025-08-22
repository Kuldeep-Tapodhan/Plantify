package com.example.plantify.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.plantify.EditProfileActivity
import com.example.plantify.LoginActivity
import com.example.plantify.R
import com.example.plantify.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        fetchUserData()

        binding.darkModeSwitch.isChecked =
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Add this click listener
        binding.editProfileButton.setOnClickListener {
            startActivity(Intent(requireActivity(), EditProfileActivity::class.java))
        }


        binding.logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun fetchUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Log.e("ProfileFragment", "User not logged in.")
            return
        }

        val uid = currentUser.uid
        val userRef = database.getReference("users").child(uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d("ProfileFragment", "Data snapshot found: ${snapshot.value}")
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val phone = snapshot.child("phone").getValue(String::class.java)
                    val location = snapshot.child("location").getValue(String::class.java)
                    val farmSize = snapshot.child("farmSize").getValue(String::class.java)
                    val primaryCrop = snapshot.child("primaryCrop").getValue(String::class.java)
                    val imageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                    binding.nameTextView.text = name ?: "N/A"
                    binding.emailTextView.text = email ?: "N/A"
                    binding.phoneTextView.text = phone ?: "N/A"
                    binding.locationTextView.text = location ?: "N/A"
                    binding.farmSizeTextView.text = farmSize ?: "N/A"
                    binding.cropTextView.text = "Primary Crop: ${primaryCrop ?: "N/A"}"

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this@ProfileFragment)
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(binding.profileImageView)
                    } else {
                        binding.profileImageView.setImageResource(R.drawable.ic_profile)
                    }
                } else {
                     Log.w("ProfileFragment", "Data snapshot does not exist for user: $uid")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileFragment", "Database error: ${error.message}")
                Toast.makeText(context, "Failed to load user data.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}