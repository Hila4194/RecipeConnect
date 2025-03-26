package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeconnect.R
import com.example.recipeconnect.adapters.RecipeAdapter
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth

class FavoriteRecipesActivity : AppCompatActivity() {

    private lateinit var favoriteRecipesRecyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter

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

        // RecyclerView setup
        favoriteRecipesRecyclerView = findViewById(R.id.favoriteRecipesRecyclerView)
        favoriteRecipesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Prepare email map for current user
        val emailMap = mutableMapOf<String, String>()
        auth.currentUser?.let { user ->
            emailMap[user.uid] = user.email ?: "Unknown"
        }

        adapter = RecipeAdapter(emptyList(), emailMap, this) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }

        favoriteRecipesRecyclerView.adapter = adapter

        // Observe all recipes and favorite recipe IDs
        val userId = auth.currentUser?.uid ?: return
        recipeViewModel.allRecipes.observe(this, Observer { allRecipes ->
            recipeViewModel.getFavoriteRecipeIds(userId) { favoriteIds ->
                val filtered = allRecipes.filter { it.id in favoriteIds }
                favoriteRecipes.clear()
                favoriteRecipes.addAll(filtered)
                adapter.updateRecipes(favoriteRecipes)
            }
        })
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