package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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

        // Setup custom toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        favoriteRecipesRecyclerView = findViewById(R.id.favoriteRecipesRecyclerView)
        favoriteRecipesRecyclerView.layoutManager = LinearLayoutManager(this)

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