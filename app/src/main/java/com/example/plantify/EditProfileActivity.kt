package com.example.plantify

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.plantify.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage

    private var imageUri: Uri? = null
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.profileImageView.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        val uid = auth.currentUser?.uid
        require(uid != null) { "User not logged in" }
        database = FirebaseDatabase.getInstance().getReference("users").child(uid)

        loadUserData()

        binding.profileImageView.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        binding.changePhotoText.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.saveButton.setOnClickListener {
            updateProfile()
        }
    }

    private fun loadUserData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    binding.nameEditText.setText(snapshot.child("name").getValue(String::class.java))
                    binding.phoneEditText.setText(snapshot.child("phone").getValue(String::class.java))
                    binding.locationEditText.setText(snapshot.child("location").getValue(String::class.java))
                    binding.farmSizeEditText.setText(snapshot.child("farmSize").getValue(String::class.java))
                    binding.cropEditText.setText(snapshot.child("primaryCrop").getValue(String::class.java))

                    val imageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this@EditProfileActivity).load(imageUrl).into(binding.profileImageView)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditProfileActivity, "Failed to load data.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateProfile() {
        val name = binding.nameEditText.text.toString().trim()
        val phone = binding.phoneEditText.text.toString().trim()
        val location = binding.locationEditText.text.toString().trim()
        val farmSize = binding.farmSizeEditText.text.toString().trim()
        val primaryCrop = binding.cropEditText.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || location.isEmpty() || farmSize.isEmpty() || primaryCrop.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "location" to location,
            "farmSize" to farmSize,
            "primaryCrop" to primaryCrop
        )

        if (imageUri != null) {
            // New image is selected, upload it first
            val storageRef = storage.reference.child("profile_images/${auth.currentUser!!.uid}")
            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        updates["profileImageUrl"] = uri.toString()
                        saveUpdatesToDatabase(updates)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            // No new image, just save the text updates
            saveUpdatesToDatabase(updates)
        }
    }

    private fun saveUpdatesToDatabase(updates: Map<String, Any>) {
        database.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish() // Go back to the profile screen
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }
}