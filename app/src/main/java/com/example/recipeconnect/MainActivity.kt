package com.example.recipeconnect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeconnect.activities.HomeActivity
import com.example.recipeconnect.activities.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the user is logged in
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // User is logged in → Go to HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // No user logged in → Go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // Close MainActivity
    }
}