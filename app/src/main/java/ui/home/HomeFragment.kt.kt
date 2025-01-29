package com.recipeconnect.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.recipeconnect.databinding.FragmentHomeBinding
import com.recipeconnect.viewmodel.RecipeViewModel
import com.recipeconnect.ui.home.RecipeAdapter

class `HomeFragment.kt` : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val recipeViewModel = RecipeViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Set up RecyclerView
        binding.rvRecipes.layoutManager = LinearLayoutManager(requireContext())

        // Observe and load recipes
        recipeViewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            binding.rvRecipes.adapter = RecipeAdapter(recipes) { selectedRecipe ->
                // Navigate to Recipe Detail
            }
        }

        // Fetch recipes
        recipeViewModel.fetchRecipes()

        return binding.root
    }
}
