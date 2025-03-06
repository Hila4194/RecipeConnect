package com.example.recipeconnect.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.google.firebase.firestore.FirebaseFirestore

class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var recipeImageView: ImageView
    private lateinit var recipeTitleTextView: TextView
    private lateinit var recipeIngredientsTextView: TextView
    private lateinit var recipeStepsTextView: TextView
    private lateinit var likeButton: Button
    private lateinit var saveButton: Button
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        recipeImageView = findViewById(R.id.recipeImageView)
        recipeTitleTextView = findViewById(R.id.recipeTitleTextView)
        recipeIngredientsTextView = findViewById(R.id.recipeIngredientsTextView)
        recipeStepsTextView = findViewById(R.id.recipeStepsTextView)
        likeButton = findViewById(R.id.likeButton)
        saveButton = findViewById(R.id.saveButton)

        val recipeId = intent.getStringExtra("RECIPE_ID") ?: return

        loadRecipeDetails(recipeId)

        likeButton.setOnClickListener { likeRecipe(recipeId) }
        saveButton.setOnClickListener { saveRecipe(recipeId) }
    }

    private fun loadRecipeDetails(recipeId: String) {
        firestore.collection("recipes").document(recipeId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("title") ?: "No Title"
                    val ingredients = document.getString("ingredients") ?: "No Ingredients"
                    val steps = document.getString("steps") ?: "No Steps"
                    val imageUrl = document.getString("imageUrl")

                    recipeTitleTextView.text = title
                    recipeIngredientsTextView.text = ingredients
                    recipeStepsTextView.text = steps

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).into(recipeImageView)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load recipe", Toast.LENGTH_SHORT).show()
            }
    }

    private fun likeRecipe(recipeId: String) {
        Toast.makeText(this, "Liked Recipe!", Toast.LENGTH_SHORT).show()
    }

    private fun saveRecipe(recipeId: String) {
        Toast.makeText(this, "Saved Recipe!", Toast.LENGTH_SHORT).show()
    }
}