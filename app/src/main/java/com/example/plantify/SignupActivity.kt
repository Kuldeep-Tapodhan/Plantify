package com.example.plantify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.plantify.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    // Holds the URI of the selected profile image
    private var imageUri: Uri? = null

    // ActivityResultLauncher for picking an image from the gallery
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.profileImageView.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Set listener on the profile image to launch the image picker
        binding.profileImageView.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val phone = binding.phoneEditText.text.toString().trim()
            val location = binding.locationEditText.text.toString().trim()
            val farmSize = binding.farmSizeEditText.text.toString().trim()
            val primaryCrop = binding.cropEditText.text.toString().trim()

            if (validateInputs(name, email, password, phone, location, farmSize, primaryCrop)) {
                registerUser(name, email, password, phone, location, farmSize, primaryCrop)
            }
        }

        binding.loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(
        name: String, email: String, password: String, phone: String,
        location: String, farmSize: String, primaryCrop: String
    ): Boolean {
        // Validation for all fields (as before)
        if (name.isEmpty() || email.isEmpty() || password.length < 6 || phone.isEmpty() || location.isEmpty() || farmSize.isEmpty() || primaryCrop.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Enter a valid email address"
            return false
        }
        return true
    }

    private fun registerUser(
        name: String, email: String, password: String, phone: String,
        location: String, farmSize: String, primaryCrop: String
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = firebaseAuth.currentUser?.uid
                    if (uid != null) {
                        uploadImageAndSaveData(uid, name, email, phone, location, farmSize, primaryCrop)
                    }
                } else {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun uploadImageAndSaveData(
        uid: String, name: String, email: String, phone: String,
        location: String, farmSize: String, primaryCrop: String
    ) {
        val userData = mutableMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "location" to location,
            "farmSize" to farmSize,
            "primaryCrop" to primaryCrop
        )

        // Check if a new image was selected
        if (imageUri != null) {
            val storageRef = storage.reference.child("profile_images/$uid")
            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    // Image uploaded, now get the download URL
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        userData["profileImageUrl"] = uri.toString()
                        saveDataToDatabase(uid, userData)
                    }
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("StorageError", "Image upload failed", e)
                    Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // No image selected, save data without the image URL
            saveDataToDatabase(uid, userData)
        }
    }

    private fun saveDataToDatabase(uid: String, userData: Map<String, Any>) {
        FirebaseDatabase.getInstance().getReference("users").child(uid)
            .setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}