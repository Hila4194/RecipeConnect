package com.example.recipeconnect.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class UserProfileActivity : AppCompatActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var dobEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var changeProfileImageButton: Button

    private var imageUri: Uri? = null
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        profileImageView = findViewById(R.id.profileImageView)
        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        dobEditText = findViewById(R.id.dobEditText)
        emailEditText = findViewById(R.id.emailEditText)
        bioEditText = findViewById(R.id.bioEditText)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        changeProfileImageButton = findViewById(R.id.changeProfileImageButton)

        loadUserProfile()

        // Select profile picture from gallery
        changeProfileImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        // Select Date of Birth from Calendar
        dobEditText.setOnClickListener {
            pickDateOfBirth()
        }

        // Save updated user profile
        saveButton.setOnClickListener {
            saveUserProfile()
        }

        // Cancel and return to home
        cancelButton.setOnClickListener {
            finish() // Close profile activity
        }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    firstNameEditText.setText(user?.firstName ?: "")
                    lastNameEditText.setText(user?.lastName ?: "")
                    dobEditText.setText(user?.dob ?: "")
                    emailEditText.setText(user?.email ?: "")
                    bioEditText.setText(user?.bio ?: "")

                    if (!user?.profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(user?.profileImageUrl)
                            .placeholder(R.drawable.ic_person)
                            .into(profileImageView)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    private fun pickDateOfBirth() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            dobEditText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
        }, year, month, day)

        datePicker.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data?.data != null) {
            imageUri = data.data
            profileImageView.setImageURI(imageUri)
        }
    }

    private fun saveUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val dob = dobEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            uploadProfileImage(uid, firstName, lastName, dob, email, bio)
        } else {
            saveUserData(uid, firstName, lastName, dob, email, bio, null)
        }
    }

    private fun uploadProfileImage(uid: String, firstName: String, lastName: String, dob: String, email: String, bio: String) {
        val storageRef = storage.reference.child("profile_pictures/$uid.jpg")
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveUserData(uid, firstName, lastName, dob, email, bio, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData(uid: String, firstName: String, lastName: String, dob: String, email: String, bio: String, imageUrl: String?) {
        val userData = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "dob" to dob,
            "bio" to bio,
            "profileImageUrl" to (imageUrl ?: "")
        )

        firestore.collection("users").document(uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                finish() // Close profile page after saving
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
            }
    }
}