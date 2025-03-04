package com.example.recipeconnect.activities

import android.app.DatePickerDialog
import android.app.Activity
import com.example.recipeconnect.models.User
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.recipeconnect.R
import java.util.*

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val backButton: ImageView = findViewById(R.id.backButton)
        val firstNameEditText: EditText = findViewById(R.id.firstNameEditText)
        val lastNameEditText: EditText = findViewById(R.id.lastNameEditText)
        val dobEditText: EditText = findViewById(R.id.dobEditText)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val bioEditText: EditText = findViewById(R.id.bioEditText)
        val profileImageView: ImageView = findViewById(R.id.profileImageView)
        val changeProfileImageButton: Button = findViewById(R.id.changeProfileImageButton)
        val createAccountButton: Button = findViewById(R.id.createAccountButton)

        // Back Button Functionality
        backButton.setOnClickListener {
            onBackPressed() // Go back to LoginActivity
        }

        // Select profile picture
        changeProfileImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // Select Date of Birth
        dobEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                dobEditText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            }, year, month, day)

            datePicker.show()
        }

        createAccountButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val dob = dobEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val bio = bioEditText.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid != null) {
                        if (imageUri != null) {
                            uploadProfileImage(uid, firstName, lastName, dob, email, bio)
                        } else {
                            saveUserToFirestore(uid, firstName, lastName, dob, email, bio, null)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Signup failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadProfileImage(uid: String, firstName: String, lastName: String, dob: String, email: String, bio: String) {
        val storageRef = storage.reference.child("profile_pictures/$uid.jpg")
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveUserToFirestore(uid, firstName, lastName, dob, email, bio, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToFirestore(uid: String, firstName: String, lastName: String, dob: String, email: String, bio: String, imageUrl: String?) {
        val user = User(
            uid = uid,
            firstName = firstName,
            lastName = lastName,
            dob = dob,
            email = email,
            bio = bio,
            profileImageUrl = imageUrl ?: ""
        )

        firestore.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving user", Toast.LENGTH_SHORT).show()
            }
    }
}