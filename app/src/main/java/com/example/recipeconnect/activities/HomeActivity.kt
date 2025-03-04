package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeconnect.R
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Check if the user is already logged in
        if (auth.currentUser != null) {
            // If user is logged in, go directly to the main recipes page
            startActivity(Intent(this, RecipesHomeActivity::class.java))
            finish() // Close HomeActivity so user can't go back
        } else {
            // If not logged in, show the landing page
            setContentView(R.layout.activity_home)

            val startButton = findViewById<Button>(R.id.startButton)
            startButton.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Close HomeActivity
            }
        }
    }
}