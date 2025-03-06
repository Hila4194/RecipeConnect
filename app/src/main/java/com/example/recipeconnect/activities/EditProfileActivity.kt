package com.example.recipeconnect.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button
    private var imageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImageView = findViewById(R.id.editProfileImageView)
        firstNameEditText = findViewById(R.id.editFirstName)
        lastNameEditText = findViewById(R.id.editLastName)
        emailEditText = findViewById(R.id.editEmail)
        bioEditText = findViewById(R.id.editBio)
        saveButton = findViewById(R.id.saveProfileButton)
        val changeProfileImageButton: Button = findViewById(R.id.changeProfileImageButton)

        // Load current user data
        loadUserProfile()

        // Select new profile image
        changeProfileImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        // Save profile changes
        saveButton.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            profileImageView.setImageURI(imageUri)
        }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    firstNameEditText.setText(document.getString("firstName"))
                    lastNameEditText.setText(document.getString("lastName"))
                    emailEditText.setText(document.getString("email"))
                    bioEditText.setText(document.getString("bio"))

                    val profileImageUrl = document.getString("profileImageUrl")
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(profileImageUrl).into(profileImageView)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()

        if (imageUri != null) {
            uploadProfileImage(uid, firstName, lastName, email, bio)
        } else {
            saveUserData(uid, firstName, lastName, email, bio, null)
        }
    }

    private fun uploadProfileImage(uid: String, firstName: String, lastName: String, email: String, bio: String) {
        val storageRef = storage.reference.child("profile_images/$uid.jpg")
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveUserData(uid, firstName, lastName, email, bio, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData(uid: String, firstName: String, lastName: String, email: String, bio: String, imageUrl: String?) {
        val userData = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "bio" to bio,
            "profileImageUrl" to (imageUrl ?: "")
        )

        firestore.collection("users").document(uid).update(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
            }
    }
}