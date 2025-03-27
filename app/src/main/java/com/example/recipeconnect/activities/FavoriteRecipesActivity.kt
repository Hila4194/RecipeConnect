package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeconnect.R
import com.example.recipeconnect.adapters.MyRecipeAdapter
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth

class FavoriteRecipesActivity : AppCompatActivity() {

    private lateinit var favoriteRecipesRecyclerView: RecyclerView
    private lateinit var adapter: MyRecipeAdapter
    private lateinit var noFavoritesMessage: TextView  // Reference to "No favorites yet" message

    private val recipeViewModel: RecipeViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()
    private val favoriteRecipes = mutableListOf<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_recipes)

        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize RecyclerView and TextView for "No favorites yet" message
        favoriteRecipesRecyclerView = findViewById(R.id.favoriteRecipesRecyclerView)
        favoriteRecipesRecyclerView.layoutManager = LinearLayoutManager(this)
        noFavoritesMessage = findViewById(R.id.noFavoritesMessage)

        // Adapter setup for read-only mode
        adapter = MyRecipeAdapter(
            emptyList(),
            this,
            onDeleteClick = {}, // No delete functionality for favorites
            onEditClick = {},   // No edit functionality for favorites
            onItemClick = { recipe ->
                val intent = Intent(this, RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE_ID", recipe.id)
                startActivity(intent)
            },
            isEditable = false,
            isFavorite = true
        )

        favoriteRecipesRecyclerView.adapter = adapter

        // Observe all recipes and favorite recipe IDs
        val userId = auth.currentUser?.uid ?: return
        recipeViewModel.allRecipes.observe(this, Observer { allRecipes ->
            recipeViewModel.getFavoriteRecipeIds(userId) { favoriteIds ->
                val filtered = allRecipes.filter { it.id in favoriteIds }
                favoriteRecipes.clear()
                favoriteRecipes.addAll(filtered)

                // Update the adapter with favorite recipes
                adapter.updateRecipes(favoriteRecipes)

                // Show/hide "No favorites yet" message based on the favorites list
                if (favoriteRecipes.isEmpty()) {
                    noFavoritesMessage.visibility = View.VISIBLE // Show message if no favorites
                    favoriteRecipesRecyclerView.visibility = View.GONE // Hide RecyclerView
                } else {
                    noFavoritesMessage.visibility = View.GONE // Hide message if there are favorites
                    favoriteRecipesRecyclerView.visibility = View.VISIBLE // Show RecyclerView
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.recipe_home_menu, menu) // Inflate the menu

        menu?.removeItem(R.id.menu_profile)  // Remove the menu item with ID `menu_profile`

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