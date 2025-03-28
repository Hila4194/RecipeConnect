package com.example.recipeconnect.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeconnect.R
import com.example.recipeconnect.adapters.RecipeAdapter
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipesHomeFragment : Fragment() {

    private lateinit var recipesRecyclerView: RecyclerView
    private lateinit var difficultySpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var scrollToTopButton: FloatingActionButton
    private lateinit var welcomeTextView: TextView
    private lateinit var adapter: RecipeAdapter

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val recipeViewModel: RecipeViewModel by viewModels()

    private val fullRecipeList = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_recipes_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the toolbar as the ActionBar
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        // Init views
        welcomeTextView = view.findViewById(R.id.welcomeTextView)
        recipesRecyclerView = view.findViewById(R.id.recipesRecyclerView)
        difficultySpinner = view.findViewById(R.id.difficultyFilterSpinner)
        categorySpinner = view.findViewById(R.id.categoryFilterSpinner)
        scrollToTopButton = view.findViewById(R.id.scrollToTopButton)

        requireActivity().title = "All Recipes"

        FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val first = doc.getString("firstName") ?: ""
                    val last = doc.getString("lastName") ?: ""
                    welcomeTextView.text = "Hi, $first $last 👋"
                }
        }

        recipesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val emailMap = mutableMapOf<String, String>()

        adapter = RecipeAdapter(emptyList(), emailMap, requireContext()) { recipe ->
            val action = RecipesHomeFragmentDirections
                .actionRecipesHomeFragmentToRecipeDetailFragment(recipe.id)
            findNavController().navigate(action)
        }
        recipesRecyclerView.adapter = adapter

        recipeViewModel.allRecipes.observe(viewLifecycleOwner, Observer { recipes ->
            fullRecipeList.clear()
            fullRecipeList.addAll(recipes)
            filterRecipes()
        })

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
                Toast.makeText(requireContext(), "Failed to load user emails", Toast.LENGTH_SHORT).show()
            }

        setupSpinners()

        difficultySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterRecipes()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterRecipes()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        recipesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10) scrollToTopButton.show()
                else if (dy < -10) scrollToTopButton.hide()
            }
        })

        scrollToTopButton.setOnClickListener {
            recipesRecyclerView.smoothScrollToPosition(0)
        }
    }

    private fun setupSpinners() {
        val difficultyOptions = arrayOf("All", "Easy", "Medium", "Hard")
        val categoryOptions = arrayOf("All", "Dairy", "Meat", "Chicken", "Desserts", "Asian")

        difficultySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, difficultyOptions)
        categorySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryOptions)
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

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.recipe_home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_profile -> {
                val action = RecipesHomeFragmentDirections.actionRecipesHomeFragmentToUserProfileFragment()
                findNavController().navigate(action)
                true
            }

            R.id.menu_logout -> {
                auth.signOut()
                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.loginFragment)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}