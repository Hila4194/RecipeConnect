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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class FavoriteRecipesFragment : BaseFragment() {

    private lateinit var favoriteRecipesRecyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var adapter: MyRecipeAdapter

    private val auth = FirebaseAuth.getInstance()
    private val recipeViewModel: RecipeViewModel by viewModels()
    private val favoriteRecipes = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_favorite_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up toolbar for BaseFragment menu to work (logout icon)
        val toolbar: androidx.appcompat.widget.Toolbar = view.findViewById(R.id.toolbar)
        (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.setSupportActionBar(toolbar)

        // Set the custom navigation icon and title, just like in EditRecipeFragment
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back)
        toolbar.title = "Favorite Recipes"
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()  // Handle back navigation
        }

        // Initialize RecyclerView and Adapter
        favoriteRecipesRecyclerView = view.findViewById(R.id.favoriteRecipesRecyclerView)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)

        favoriteRecipesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = MyRecipeAdapter(
            emptyList(),
            requireContext(),
            onDeleteClick = {},  // Handle delete (if necessary)
            onEditClick = {},  // Handle edit (if necessary)
            onItemClick = { recipe ->  // Handle item click (navigate to recipe detail)
                val action = FavoriteRecipesFragmentDirections
                    .actionFavoriteRecipesFragmentToRecipeDetailFragment(recipe.id)
                findNavController().navigate(action)
            },
            isEditable = false,
            isFavorite = true
        )
        favoriteRecipesRecyclerView.adapter = adapter

        // Check if the user is logged in
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "You must be logged in", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.loginFragment)
            return
        }

        // Observe recipes from the ViewModel
        recipeViewModel.allRecipes.observe(viewLifecycleOwner, Observer { recipes ->
            recipeViewModel.getFavoriteRecipeIds(currentUserId) { favIds ->
                favoriteRecipes.clear()
                favoriteRecipes.addAll(recipes.filter { it.id in favIds })
                adapter.updateRecipes(favoriteRecipes)

                // Handle empty state (no favorite recipes)
                emptyStateTextView.visibility =
                    if (favoriteRecipes.isEmpty()) View.VISIBLE else View.GONE
            }
        })
    }
}