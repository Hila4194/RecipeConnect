package com.example.recipeconnect.activities

import android.app.DatePickerDialog
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.recipeconnect.R
import com.example.recipeconnect.models.User
import com.example.recipeconnect.models.dao.UserImage
import com.example.recipeconnect.models.dao.RecipeDatabase
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val backButton: ImageView = findViewById(R.id.backButton)
        val firstNameEditText: EditText = findViewById(R.id.firstNameEditText)
        val lastNameEditText: EditText = findViewById(R.id.lastNameEditText)
        val dobEditText: EditText = findViewById(R.id.dobEditText)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val bioEditText: EditText = findViewById(R.id.bioEditText)
        val changeProfileImageButton: Button = findViewById(R.id.changeProfileImageButton)
        val createAccountButton: Button = findViewById(R.id.createAccountButton)

        backButton.setOnClickListener {
            onBackPressed()
        }

        changeProfileImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, 100)
        }

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
                        saveProfileImageLocallyAndContinue(uid, firstName, lastName, dob, email, bio)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Signup failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileImageLocallyAndContinue(
        uid: String,
        firstName: String,
        lastName: String,
        dob: String,
        email: String,
        bio: String
    ) {
        if (imageUri != null) {
            val imagePath = saveImageToInternalStorage(this, imageUri!!, "profile_$uid")
            val userImage = UserImage(uid = uid, imagePath = imagePath)

            lifecycleScope.launch {
                val db = RecipeDatabase.getDatabase(applicationContext)
                db.userImageDao().insert(userImage)
                saveUserToFirestore(uid, firstName, lastName, dob, email, bio, null)
            }
        } else {
            saveUserToFirestore(uid, firstName, lastName, dob, email, bio, null)
        }
    }

    private fun saveUserToFirestore(
        uid: String,
        firstName: String,
        lastName: String,
        dob: String,
        email: String,
        bio: String,
        imageUrl: String?
    ) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            val profileImageView = findViewById<ImageView>(R.id.profileImageView)
            Glide.with(this).load(imageUri).into(profileImageView)
        }
    }

    private fun saveImageToInternalStorage(context: Activity, uri: Uri, fileName: String): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "$fileName.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
        return file.absolutePath
    }
}