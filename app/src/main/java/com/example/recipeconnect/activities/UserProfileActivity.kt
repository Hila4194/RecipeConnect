package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileActivity : AppCompatActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var firstNameTextView: EditText
    private lateinit var lastNameTextView: EditText
    private lateinit var dobTextView: EditText
    private lateinit var emailTextView: EditText
    private lateinit var bioTextView: EditText
    private lateinit var editProfileButton: Button
    private lateinit var myRecipesButton: Button
    private lateinit var favoriteRecipesButton: Button

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Set up the toolbar for the back button
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the up navigation (back button)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        profileImageView = findViewById(R.id.profileImageView)
        firstNameTextView = findViewById(R.id.firstNameTextView)
        lastNameTextView = findViewById(R.id.lastNameTextView)
        dobTextView = findViewById(R.id.dobTextView)
        emailTextView = findViewById(R.id.emailEditText)
        bioTextView = findViewById(R.id.bioTextView)
        editProfileButton = findViewById(R.id.editProfileButton)
        myRecipesButton = findViewById(R.id.myRecipesButton)
        favoriteRecipesButton = findViewById(R.id.favoriteRecipesButton)

        loadUserProfile()

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        myRecipesButton.setOnClickListener {
            startActivity(Intent(this, MyRecipesActivity::class.java))
        }

        favoriteRecipesButton.setOnClickListener {
            startActivity(Intent(this, FavoriteRecipesActivity::class.java))
        }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    firstNameTextView.setText(user?.firstName ?: "N/A")
                    lastNameTextView.setText(user?.lastName ?: "N/A")
                    dobTextView.setText(user?.dob ?: "N/A")
                    emailTextView.setText(user?.email ?: "N/A")
                    bioTextView.setText(user?.bio ?: "No bio available")
                    Glide.with(this).load(user?.profileImageUrl).into(profileImageView)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    // Handle back button click
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // When back button is clicked, navigate back to the previous screen
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}