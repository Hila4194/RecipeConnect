package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class MyRecipesActivity : AppCompatActivity() {

    private lateinit var myRecipesRecyclerView: RecyclerView
    private lateinit var addRecipeButton: Button
    private lateinit var scrollToTopButton: FloatingActionButton
    private lateinit var emptyStateTextView: TextView
    private lateinit var adapter: MyRecipeAdapter

    private val auth = FirebaseAuth.getInstance()
    private val recipeViewModel: RecipeViewModel by viewModels()
    private val myRecipeList = mutableListOf<Recipe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_recipes)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Views
        myRecipesRecyclerView = findViewById(R.id.myRecipesRecyclerView)
        myRecipesRecyclerView.layoutManager = LinearLayoutManager(this)

        scrollToTopButton = findViewById(R.id.scrollToTopButton)
        addRecipeButton = findViewById(R.id.addRecipeButton)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)

        // Adapter
        adapter = MyRecipeAdapter(
            emptyList(),
            this,
            onDeleteClick = { recipe ->
                recipeViewModel.delete(recipe)
            },
            onEditClick = { recipe ->
                val intent = Intent(this, EditMyRecipeActivity::class.java)
                intent.putExtra("RECIPE_ID", recipe.id)
                startActivity(intent)
            },
            onItemClick = { recipe ->
                val intent = Intent(this, RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE_ID", recipe.id)
                startActivity(intent)
            }
        )
        myRecipesRecyclerView.adapter = adapter

        // Observe only the current user's recipes
        val currentUserId = auth.currentUser?.uid
        recipeViewModel.allRecipes.observe(this) { recipes ->
            val myRecipes = recipes.filter { it.userId == currentUserId }
            myRecipeList.clear()
            myRecipeList.addAll(myRecipes)
            adapter.updateRecipes(myRecipeList)

            // Show/hide empty message
            emptyStateTextView.visibility =
                if (myRecipeList.isEmpty()) View.VISIBLE else View.GONE
        }

        // Add Recipe Button
        addRecipeButton.setOnClickListener {
            startActivity(Intent(this, AddRecipeActivity::class.java))
        }

        // Scroll-to-top button logic
        myRecipesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10) scrollToTopButton.show()
                else if (dy < -10) scrollToTopButton.hide()
            }
        })

        scrollToTopButton.setOnClickListener {
            myRecipesRecyclerView.smoothScrollToPosition(0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.recipe_home_menu, menu)
        menu?.removeItem(R.id.menu_profile)
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