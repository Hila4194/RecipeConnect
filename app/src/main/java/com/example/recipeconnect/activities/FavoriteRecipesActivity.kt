package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeconnect.R
import com.example.recipeconnect.adapters.RecipeAdapter
import com.example.recipeconnect.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteRecipesActivity : AppCompatActivity() {
    private lateinit var favoriteRecipesRecyclerView: RecyclerView
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val recipeList = mutableListOf<Recipe>()
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_recipes)

        favoriteRecipesRecyclerView = findViewById(R.id.favoriteRecipesRecyclerView)
        favoriteRecipesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set the adapter to the RecyclerView with a click listener for each item
        adapter = RecipeAdapter(recipeList) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        favoriteRecipesRecyclerView.adapter = adapter

        loadFavoriteRecipes()
    }

    private fun loadFavoriteRecipes() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("favorites")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { documents ->
                recipeList.clear()
                for (document in documents) {
                    val recipeId = document.getString("recipeId") ?: continue
                    firestore.collection("recipes").document(recipeId).get()
                        .addOnSuccessListener { recipeDoc ->
                            val recipe = recipeDoc.toObject(Recipe::class.java)?.apply { id = recipeDoc.id }
                            recipe?.let { recipeList.add(it) }
                            adapter.notifyDataSetChanged()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load favorite recipes", Toast.LENGTH_SHORT).show()
            }
    }
}