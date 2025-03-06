package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeconnect.R
import com.example.recipeconnect.adapters.RecipeAdapter // Make sure this import is correct
import com.example.recipeconnect.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyRecipesActivity : AppCompatActivity() {
    private lateinit var myRecipesRecyclerView: RecyclerView
    private lateinit var addRecipeButton: Button  // Declare the Add Recipe button
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val recipeList = mutableListOf<Recipe>() // Ensure this list is initialized
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_recipes)

        // Initialize RecyclerView
        myRecipesRecyclerView = findViewById(R.id.myRecipesRecyclerView)
        myRecipesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with an empty list of recipes
        adapter = RecipeAdapter(recipeList) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }

        // Set the adapter to the RecyclerView
        myRecipesRecyclerView.adapter = adapter

        // Load recipes from Firestore
        loadMyRecipes()

        // Initialize and set up the "Add Recipe" button
        addRecipeButton = findViewById(R.id.addRecipeButton)  // Get the button from the layout
        addRecipeButton.setOnClickListener {
            // Navigate to AddRecipeActivity when button is clicked
            val intent = Intent(this, AddRecipeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadMyRecipes() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("recipes")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { documents ->
                recipeList.clear() // Clear existing list before adding new recipes
                for (document in documents) {
                    val recipe = document.toObject(Recipe::class.java).apply { id = document.id }
                    recipeList.add(recipe)
                }
                adapter.notifyDataSetChanged() // Notify adapter that data has changed
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load recipes", Toast.LENGTH_SHORT).show()
            }
    }
}