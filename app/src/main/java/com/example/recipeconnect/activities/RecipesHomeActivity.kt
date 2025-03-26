package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeconnect.R
import com.example.recipeconnect.adapters.RecipeAdapter
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipesHomeActivity : AppCompatActivity() {

    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var difficultySpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var scrollToTopButton: FloatingActionButton
    private lateinit var adapter: RecipeAdapter

    private val auth = FirebaseAuth.getInstance()
    private val fullRecipeList = mutableListOf<Recipe>()

    private val recipeViewModel: RecipeViewModel by viewModels()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes_home)

        // Set the Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize views
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView)
        difficultySpinner = findViewById(R.id.difficultyFilterSpinner)
        categorySpinner = findViewById(R.id.categoryFilterSpinner)
        scrollToTopButton = findViewById(R.id.scrollToTopButton)

        recipesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Prepare initial (empty) email map and adapter
        val emailMap = mutableMapOf<String, String>()

        adapter = RecipeAdapter(emptyList(), emailMap, this) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        recipesRecyclerView.adapter = adapter

        // Observe recipes from ViewModel (Room DB)
        recipeViewModel.allRecipes.observe(this, Observer { recipes ->
            fullRecipeList.clear()
            fullRecipeList.addAll(recipes)
            filterRecipes()
        })

        // Load user emails from Firestore
        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val uid = document.getString("uid")
                    val email = document.getString("email")
                    if (!uid.isNullOrEmpty() && !email.isNullOrEmpty()) {
                        emailMap[uid] = email
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user emails", Toast.LENGTH_SHORT).show()
            }

        // Set filter listeners
        setupSpinners()

        difficultySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filterRecipes()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filterRecipes()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Scroll-to-top button logic
        recipesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10) {
                    scrollToTopButton.show()
                } else if (dy < -10) {
                    scrollToTopButton.hide()
                }
            }
        })

        scrollToTopButton.setOnClickListener {
            recipesRecyclerView.smoothScrollToPosition(0)
        }
    }

    private fun setupSpinners() {
        val difficultyOptions = arrayOf("All", "Easy", "Medium", "Hard")
        val categoryOptions = arrayOf("All", "Dairy", "Meat", "Chicken", "Desserts", "Asian")

        difficultySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, difficultyOptions)
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions)
    }

    private fun filterRecipes() {
        val selectedDifficulty = difficultySpinner.selectedItem.toString()
        val selectedCategory = categorySpinner.selectedItem.toString()

        val filteredList = fullRecipeList.filter { recipe ->
            val matchesDifficulty = selectedDifficulty == "All" || recipe.difficulty == selectedDifficulty
            val matchesCategory = selectedCategory == "All" || recipe.category == selectedCategory
            matchesDifficulty && matchesCategory
        }

        adapter.updateRecipes(filteredList)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.recipe_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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