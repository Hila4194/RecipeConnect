package com.example.recipeconnect.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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

class MyRecipesFragment : BaseFragment() {

    // UI components
    private lateinit var myRecipesRecyclerView: RecyclerView
    private lateinit var addRecipeButton: Button
    private lateinit var scrollToTopButton: FloatingActionButton
    private lateinit var emptyStateTextView: TextView
    private lateinit var adapter: MyRecipeAdapter

    // Firebase auth + ViewModel
    private val auth = FirebaseAuth.getInstance()
    private val recipeViewModel: RecipeViewModel by viewModels()
    private val myRecipeList = mutableListOf<Recipe>()

    // Inflate layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_recipes, container, false)
    }

    // Setup toolbar, adapter, observers and interactions
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up toolbar to enable BaseFragment menu (logout icon)
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(toolbar)

        // Configure navigation and title styling
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back)
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Bind views
        myRecipesRecyclerView = view.findViewById(R.id.myRecipesRecyclerView)
        myRecipesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        scrollToTopButton = view.findViewById(R.id.scrollToTopButton)
        addRecipeButton = view.findViewById(R.id.addRecipeButton)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)

        // Setup adapter with delete/edit/view handlers
        adapter = MyRecipeAdapter(
            emptyList(),
            requireContext(),
            onDeleteClick = { recipe -> recipeViewModel.delete(recipe) },
            onEditClick = { recipe ->
                val action = MyRecipesFragmentDirections
                    .actionMyRecipesFragmentToEditMyRecipeFragment(recipe.id)
                findNavController().navigate(action)
            },
            onItemClick = { recipe ->
                val action = MyRecipesFragmentDirections
                    .actionMyRecipesFragmentToRecipeDetailFragment(recipe.id)
                findNavController().navigate(action)
            },
            isEditable = true,
            isFavorite = false
        )
        myRecipesRecyclerView.adapter = adapter

        // Filter and observe only the current user's recipes
        val currentUserId = auth.currentUser?.uid
        recipeViewModel.allRecipes.observe(viewLifecycleOwner, Observer { recipes ->
            val myRecipes = recipes.filter { it.userId == currentUserId }
            myRecipeList.clear()
            myRecipeList.addAll(myRecipes)
            adapter.updateRecipes(myRecipeList)

            // Toggle empty state message
            emptyStateTextView.visibility =
                if (myRecipeList.isEmpty()) View.VISIBLE else View.GONE
        })

        // Navigate to AddRecipeFragment when "+" is clicked
        addRecipeButton.setOnClickListener {
            findNavController().navigate(R.id.addRecipeFragment)
        }

        // Show/hide "scroll to top" button based on scroll direction
        myRecipesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10) scrollToTopButton.show() // Scrolling down
                else if (dy < -10) scrollToTopButton.hide() // Scrolling up
            }
        })

        // Scroll to top smoothly when button clicked
        scrollToTopButton.setOnClickListener {
            myRecipesRecyclerView.smoothScrollToPosition(0)
        }
    }
}