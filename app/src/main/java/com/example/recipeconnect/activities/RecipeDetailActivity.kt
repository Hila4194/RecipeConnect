package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var recipeImageView: ImageView
    private lateinit var recipeTitleTextView: TextView
    private lateinit var recipeIngredientsTextView: TextView
    private lateinit var recipeStepsTextView: TextView
    private lateinit var likeButton: Button
    private lateinit var saveButton: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recipeImageView = findViewById(R.id.recipeImageView)
        recipeTitleTextView = findViewById(R.id.recipeTitleTextView)
        recipeIngredientsTextView = findViewById(R.id.recipeIngredientsTextView)
        recipeStepsTextView = findViewById(R.id.recipeStepsTextView)
        likeButton = findViewById(R.id.likeButton)
        saveButton = findViewById(R.id.saveButton)

        val recipeId = intent.getStringExtra("RECIPE_ID")
        if (recipeId.isNullOrEmpty()) {
            Toast.makeText(this, "No recipe found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadRecipeDetails(recipeId)

        likeButton.setOnClickListener { likeRecipe(recipeId) }
        saveButton.setOnClickListener { saveRecipe(recipeId) }
    }

    private fun loadRecipeDetails(recipeId: String) {
        firestore.collection("recipes").document(recipeId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("title") ?: "No Title"
                    val ingredientsList = document.get("ingredients") as? List<String> ?: listOf("No ingredients")
                    val ingredients = ingredientsList.joinToString(", ")
                    val steps = document.getString("steps") ?: "No Steps"
                    val imageUrl = document.getString("imageUrl")

                    recipeTitleTextView.text = title
                    recipeIngredientsTextView.text = ingredients
                    recipeStepsTextView.text = steps

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.default_recipe)
                            .centerCrop()
                            .into(recipeImageView)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.recipe_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.menu_profile -> {
                startActivity(Intent(this, UserProfileActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}