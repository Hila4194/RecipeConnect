package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeconnect.R
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        // Check if a user is already logged in
        if (auth.currentUser != null) {
            startActivity(Intent(this, RecipesHomeActivity::class.java))
            finish() // Close HomeActivity so the user can't go back to it
        } else {
            // Otherwise, go to the login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}