package com.example.recipeconnect.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.models.dao.FavoriteRecipeEntity
import com.example.recipeconnect.models.dao.RecipeDatabase
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class RecipeDetailFragment : Fragment() {

    private val args: RecipeDetailFragmentArgs by navArgs() // SafeArgs: get recipeId passed in navigation

    // UI elements
    private lateinit var recipeImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var creatorTextView: TextView
    private lateinit var prepTimeTextView: TextView
    private lateinit var ingredientsTextView: TextView
    private lateinit var stepsTextView: TextView
    private lateinit var likeIcon: ImageButton
    private lateinit var caloriesTextView: TextView
    private lateinit var caloriesSpinner: ProgressBar
    private lateinit var nutritionixAttribution: TextView

    // ViewModel & Firebase instances
    private val recipeViewModel: RecipeViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Local Room DAO for favorites
    private val favoriteDao by lazy {
        RecipeDatabase.getDatabase(requireContext()).favoriteRecipeDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar back navigation
        val toolbar: androidx.appcompat.widget.Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Bind UI components
        recipeImageView = view.findViewById(R.id.recipeImageView)
        titleTextView = view.findViewById(R.id.recipeTitleTextView)
        creatorTextView = view.findViewById(R.id.recipeCreatorTextView)
        prepTimeTextView = view.findViewById(R.id.recipePrepTimeTextView)
        ingredientsTextView = view.findViewById(R.id.recipeIngredientsTextView)
        stepsTextView = view.findViewById(R.id.recipeStepsTextView)
        likeIcon = view.findViewById(R.id.likeIcon)
        caloriesTextView = view.findViewById(R.id.caloriesTextView)
        caloriesSpinner = view.findViewById(R.id.caloriesLoadingSpinner)
        nutritionixAttribution = view.findViewById(R.id.nutritionixAttributionTextView)
        nutritionixAttribution.visibility = View.GONE

        // Observe Nutritionix calorie data
        recipeViewModel.nutritionLiveData.observe(viewLifecycleOwner) { foods ->
            caloriesSpinner.visibility = View.GONE
            nutritionixAttribution.visibility = View.VISIBLE

            if (foods.isNotEmpty()) {
                val totalCalories = foods.sumOf { it.nf_calories }
                val breakdown = foods.joinToString("\n") {
                    "${it.food_name}: ${it.nf_calories.toInt()} kcal"
                }
                caloriesTextView.text = "Total: ${totalCalories.toInt()} kcal\n$breakdown"
            } else {
                caloriesTextView.text = "No calorie data available"
            }
        }

        // Load and show recipe details
        val recipeId = args.recipeId
        recipeViewModel.getRecipeById(recipeId) { recipe ->
            recipe?.let { showRecipeDetails(it) }
        }
    }

    // Displays the full details of the selected recipe
    private fun showRecipeDetails(recipe: Recipe) {
        titleTextView.text = recipe.title
        prepTimeTextView.text = recipe.prepTime
        ingredientsTextView.text = recipe.ingredients.joinToString(", ")
        stepsTextView.text = recipe.steps

        // Load image from local or URL with placeholder fallback
        Glide.with(this)
            .load(recipe.imageUrl)
            .placeholder(R.drawable.default_recipe)
            .error(R.drawable.default_recipe)
            .centerCrop()
            .into(recipeImageView)

        fetchCreatorEmail(recipe.userId)
        updateLikeIcon(recipe.id)

        // Request calorie info from Nutritionix
        val query = recipe.ingredients.joinToString(", ")
        caloriesSpinner.visibility = View.VISIBLE
        nutritionixAttribution.visibility = View.GONE
        recipeViewModel.fetchCalories(query)

        // Toggle favorite (like) status on click
        likeIcon.setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Snackbar.make(requireView(), "You must be logged in", Snackbar.LENGTH_SHORT)
                    .setAnchorView(likeIcon)
                    .show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val isFavorite = favoriteDao.isFavorite(userId, recipe.id)
                if (isFavorite) {
                    // Remove from favorites
                    favoriteDao.removeFromFavorites(FavoriteRecipeEntity(recipeId = recipe.id, userId = userId))
                } else {
                    // Add to favorites
                    favoriteDao.addToFavorites(FavoriteRecipeEntity(recipeId = recipe.id, userId = userId))
                }
                // Refresh like icon on UI thread
                withContext(Dispatchers.Main) {
                    updateLikeIcon(recipe.id)
                }
            }
        }
    }

    // Update like icon (filled vs outline) depending on favorite status
    private fun updateLikeIcon(recipeId: String) {
        val userId = auth.currentUser?.uid ?: return
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

    // Get the recipe creator's email from Firestore
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