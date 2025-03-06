package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeconnect.R
import com.example.recipeconnect.adapters.RecipeAdapter
import com.example.recipeconnect.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipesHomeActivity : AppCompatActivity() {
    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var difficultySpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val recipeList = mutableListOf<Recipe>()
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes_home)

        // Set the Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize RecyclerView
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView)
        recipesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Adapter with an empty list (it will be updated later)
        adapter = RecipeAdapter(recipeList) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        recipesRecyclerView.adapter = adapter

        // Initialize Filter Spinners
        difficultySpinner = findViewById(R.id.difficultyFilterSpinner)
        categorySpinner = findViewById(R.id.categoryFilterSpinner)

        setupSpinners()

        // Load all recipes initially
        loadRecipes()

        // Set filter listeners
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

        // Logout Button
        val logoutButton: Button = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    // Initialize spinners with the required values
    private fun setupSpinners() {
        val difficultyOptions = arrayOf("All", "Easy", "Medium", "Hard")
        val categoryOptions = arrayOf("All", "Dairy", "Meat", "Chicken", "Desserts", "Asian")

        difficultySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, difficultyOptions)
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryOptions)
    }

    // Load recipes from Firestore
    private fun loadRecipes() {
        firestore.collection("recipes").get()
            .addOnSuccessListener { documents ->
                recipeList.clear()
                for (document in documents) {
                    val recipe = document.toObject(Recipe::class.java).apply { id = document.id }
                    recipeList.add(recipe)
                }
                filterRecipes() // Apply filtering immediately
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load recipes", Toast.LENGTH_SHORT).show()
            }
    }

    // Filter recipes based on selected difficulty and category
    private fun filterRecipes() {
        val selectedDifficulty = difficultySpinner.selectedItem.toString()
        val selectedCategory = categorySpinner.selectedItem.toString()

        val filteredList = recipeList.filter { recipe ->
            val matchesDifficulty = selectedDifficulty == "All" || recipe.difficulty == selectedDifficulty
            val matchesCategory = selectedCategory == "All" || recipe.category == selectedCategory
            matchesDifficulty && matchesCategory
        }

        // Update the adapter with filtered recipes
        adapter = RecipeAdapter(filteredList) { recipe ->
            val intent = Intent(this, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            startActivity(intent)
        }
        recipesRecyclerView.adapter = adapter
    }

    // Inflate the options menu (Profile and Logout)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.recipe_home_menu, menu)
        return true
    }

    // Handle menu item clicks (Profile and Logout)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_profile -> {
                startActivity(Intent(this, UserProfileActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}