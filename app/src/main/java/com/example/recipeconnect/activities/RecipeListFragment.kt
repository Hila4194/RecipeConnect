package com.example.recipeconnect.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeconnect.R
import com.example.recipeconnect.adapters.RecipeAdapter
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class RecipeListFragment : Fragment() {

    private lateinit var recipeViewModel: RecipeViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_recipe_list, container, false)

        recyclerView = view.findViewById(R.id.recipeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val emailMap = mutableMapOf<String, String>()
        FirebaseAuth.getInstance().currentUser?.let { user ->
            emailMap[user.uid] = user.email ?: "Unknown"
        }

        adapter = RecipeAdapter(emptyList(), emailMap, requireContext()) { /* no-op */ }
        recyclerView.adapter = adapter

        recipeViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[RecipeViewModel::class.java]

        recipeViewModel.allRecipes.observe(viewLifecycleOwner) { recipes ->
            if (::adapter.isInitialized) adapter.updateRecipes(recipes)
        }

        val testRecipe = Recipe(
            id = UUID.randomUUID().toString(),
            title = "Test Pancake",
            ingredients = listOf("Flour", "Egg", "Milk"),
            steps = "Mix and cook",
            imageUrl = "",
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user",
            prepTime = "15 mins",
            difficulty = "Easy",
            category = "Desserts"
        )
        recipeViewModel.insert(testRecipe)

        return view
    }
}