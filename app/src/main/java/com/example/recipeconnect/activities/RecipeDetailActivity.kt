package com.example.recipeconnect.activities

import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.models.dao.FavoriteRecipeEntity
import com.example.recipeconnect.models.dao.RecipeDatabase
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var recipeImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var creatorTextView: TextView
    private lateinit var prepTimeTextView: TextView
    private lateinit var ingredientsTextView: TextView
    private lateinit var stepsTextView: TextView
    private lateinit var likeIcon: ImageButton

    private val recipeViewModel: RecipeViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()
    private val favoriteDao by lazy {
        RecipeDatabase.getDatabase(this).favoriteRecipeDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Bind views
        recipeImageView = findViewById(R.id.recipeImageView)
        titleTextView = findViewById(R.id.recipeTitleTextView)
        creatorTextView = findViewById(R.id.recipeCreatorTextView)
        prepTimeTextView = findViewById(R.id.recipePrepTimeTextView)
        ingredientsTextView = findViewById(R.id.recipeIngredientsTextView)
        stepsTextView = findViewById(R.id.recipeStepsTextView)
        likeIcon = findViewById(R.id.likeIcon)

        val recipeId = intent.getStringExtra("RECIPE_ID")
        if (recipeId == null) {
            Toast.makeText(this, "Missing recipe ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recipeViewModel.getRecipeById(recipeId) { recipe ->
            recipe?.let { showRecipeDetails(it) }
        }
    }

    private fun showRecipeDetails(recipe: Recipe) {
        titleTextView.text = recipe.title
        prepTimeTextView.text = recipe.prepTime
        ingredientsTextView.text = recipe.ingredients.joinToString(", ")
        stepsTextView.text = recipe.steps

        Glide.with(this)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.default_recipe)
            .error(R.drawable.default_recipe)
            .centerCrop()
            .into(recipeImageView)

        fetchCreatorEmail(recipe.userId)
        updateLikeIcon(recipe.id)

        likeIcon.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val isFavorite = favoriteDao.isFavorite(userId, recipe.id)
                if (isFavorite) {
                    favoriteDao.removeFromFavorites(FavoriteRecipeEntity(recipeId = recipe.id, userId = userId))
                } else {
                    favoriteDao.addToFavorites(FavoriteRecipeEntity(recipeId = recipe.id, userId = userId))
                }
                withContext(Dispatchers.Main) {
                    updateLikeIcon(recipe.id)
                }
            }
        }
    }

    private fun updateLikeIcon(recipeId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val isFavorite = favoriteDao.isFavorite(userId, recipeId)
            withContext(Dispatchers.Main) {
                likeIcon.setImageResource(
                    if (isFavorite) R.drawable.ic_heart_filled
                    else R.drawable.ic_heart_outline
                )
            }
        }
    }

    private fun fetchCreatorEmail(userId: String) {
        db.collection("users")
            .whereEqualTo("uid", userId)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val email = result.documents.firstOrNull()?.getString("email")
                creatorTextView.text = "By: ${email ?: "Unknown"}"
            }
            .addOnFailureListener {
                creatorTextView.text = "By: Unknown"
            }
    }
}