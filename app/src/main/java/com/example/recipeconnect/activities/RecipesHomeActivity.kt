package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.recipeconnect.R

class RecipesHomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var welcomeTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes_home)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        welcomeTextView = findViewById(R.id.welcomeTextView)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        // Fetch and display the username
        loadUsername()

        // Logout button
        logoutButton.setOnClickListener {
            auth.signOut() // Sign out from Firebase
            startActivity(Intent(this, HomeActivity::class.java)) // Redirect to landing page
            finish() // Close RecipesHomeActivity
        }
    }

    private fun loadUsername() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName") ?: "User"
                    welcomeTextView.text = "Welcome, $firstName!"
                }
            }
            .addOnFailureListener {
                welcomeTextView.text = "Welcome!"
            }
    }
}