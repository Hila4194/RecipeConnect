package com.example.recipeconnect.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeconnect.R
import com.example.recipeconnect.adapters.MyRecipeAdapter
import com.example.recipeconnect.base.BaseFragment
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class FavoriteRecipesFragment : BaseFragment() {

    // UI elements
    private lateinit var favoriteRecipesRecyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var adapter: MyRecipeAdapter

    // Auth and data
    private val auth = FirebaseAuth.getInstance()
    private val recipeViewModel: RecipeViewModel by viewModels()
    private val favoriteRecipes = mutableListOf<Recipe>()

    // Inflate layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_favorite_recipes, container, false)
    }

    // Set up toolbar, adapter, and data observers
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up toolbar with back navigation
        val toolbar: androidx.appcompat.widget.Toolbar = view.findViewById(R.id.toolbar)
        (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.setSupportActionBar(toolbar)
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back)
        toolbar.title = "Favorite Recipes"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize RecyclerView and empty-state message
        favoriteRecipesRecyclerView = view.findViewById(R.id.favoriteRecipesRecyclerView)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)

        favoriteRecipesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up adapter in non-editable, favorite mode
        adapter = MyRecipeAdapter(
            emptyList(),
            requireContext(),
            onDeleteClick = {}, // Deletion not available in this screen
            onEditClick = {},   // Editing not available either
            onItemClick = { recipe ->
                // Navigate to recipe detail screen when item clicked
                val action = FavoriteRecipesFragmentDirections
                    .actionFavoriteRecipesFragmentToRecipeDetailFragment(recipe.id)
                findNavController().navigate(action)
            },
            isEditable = false,
            isFavorite = true
        )
        favoriteRecipesRecyclerView.adapter = adapter

        // If user is not logged in, redirect to login
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Snackbar.make(requireView(), "You must be logged in", Snackbar.LENGTH_SHORT)
                .setAnchorView(emptyStateTextView)
                .show()
            findNavController().navigate(R.id.loginFragment)
            return
        }

        // Observe all recipes and filter only favorites
        recipeViewModel.allRecipes.observe(viewLifecycleOwner, Observer { recipes ->
            recipeViewModel.getFavoriteRecipeIds(currentUserId) { favIds ->
                // Filter and display favorite recipes
                favoriteRecipes.clear()
                favoriteRecipes.addAll(recipes.filter { it.id in favIds })
                adapter.updateRecipes(favoriteRecipes)

                // Show empty-state text if no favorites
                emptyStateTextView.visibility =
                    if (favoriteRecipes.isEmpty()) View.VISIBLE else View.GONE
            }
        })
    }
}