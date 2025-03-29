package com.example.recipeconnect.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.base.BaseFragment
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.util.*

class AddRecipeFragment : BaseFragment() {

    // UI elements
    private lateinit var recipeImageView: ImageView
    private lateinit var recipeTitleEditText: EditText
    private lateinit var prepTimeEditText: EditText
    private lateinit var difficultySpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var ingredientsEditText: EditText
    private lateinit var stepsEditText: EditText
    private lateinit var saveRecipeButton: Button
    private var imageUri: Uri? = null

    // Firebase & ViewModel setup
    private val auth = FirebaseAuth.getInstance()
    private val recipeViewModel: RecipeViewModel by viewModels()

    // Inflates the layout for the AddRecipeFragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_recipe, container, false)
    }

    // Handles UI setup and button click listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up custom toolbar with back navigation
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize UI components
        recipeImageView = view.findViewById(R.id.recipeImageView)
        recipeTitleEditText = view.findViewById(R.id.recipeTitleEditText)
        prepTimeEditText = view.findViewById(R.id.prepTimeEditText)
        difficultySpinner = view.findViewById(R.id.difficultySpinner)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        ingredientsEditText = view.findViewById(R.id.ingredientsEditText)
        stepsEditText = view.findViewById(R.id.stepsEditText)
        saveRecipeButton = view.findViewById(R.id.saveRecipeButton)

        // Load spinner values
        setupSpinners()

        // Handle image selection
        view.findViewById<Button>(R.id.selectImageButton).setOnClickListener {
            selectImageFromGallery()
        }

        // Handle recipe save
        saveRecipeButton.setOnClickListener {
            validateAndSaveRecipe()
        }
    }

    // Fills difficulty and category spinners with predefined values
    private fun setupSpinners() {
        val difficulties = arrayOf("Easy", "Medium", "Hard")
        val categories = arrayOf("Dairy", "Meat", "Chicken", "Desserts", "Asian", "Italian", "Mexican", "Vegan", "Mediterranean", "Snacks")

        difficultySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, difficulties)
        categorySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
    }

    // Validates the form, saves image if needed, creates and inserts Recipe object into DB
    private fun validateAndSaveRecipe() {
        val scrollView = requireView().findViewById<ScrollView>(R.id.addRecipeScrollView)
        val progressBar = requireView().findViewById<ProgressBar>(R.id.saveProgressBar)

        // Extract user inputs
        val title = recipeTitleEditText.text.toString().trim()
        val prepTime = prepTimeEditText.text.toString().trim()
        val difficulty = difficultySpinner.selectedItem.toString()
        val category = categorySpinner.selectedItem.toString()
        val ingredientsText = ingredientsEditText.text.toString().trim()
        val ingredients = ingredientsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val steps = stepsEditText.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        // Basic validation
        if (title.isEmpty() || prepTime.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
            Snackbar.make(requireView(), "Please fill in all required fields", Snackbar.LENGTH_SHORT).show()
            return
        }

        // UI loading state
        progressBar.visibility = View.VISIBLE
        scrollView.alpha = 0.5f

        // Save image to internal storage if selected
        val imageUrl = if (imageUri != null) {
            saveImageToInternalStorage(imageUri!!, UUID.randomUUID().toString())
        } else ""

        // Create and save recipe object
        val recipe = Recipe(
            id = UUID.randomUUID().toString(),
            title = title,
            prepTime = prepTime,
            difficulty = difficulty,
            category = category,
            ingredients = ingredients,
            steps = steps,
            imageUrl = imageUrl,
            userId = userId
        )

        recipeViewModel.insert(recipe)

        // Restore UI state and navigate back
        progressBar.visibility = View.GONE
        scrollView.alpha = 1f

        Snackbar.make(requireView(), "Recipe saved!", Snackbar.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    // Launches intent to let user pick an image from the gallery
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, 100)
    }

    // Receives the result of the image selection and displays it using Glide
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            Glide.with(this).load(imageUri).into(recipeImageView)
        }
    }

    // Copies selected image to internal storage and returns the absolute path
    private fun saveImageToInternalStorage(uri: Uri, fileName: String): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().filesDir, "$fileName.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
        return file.absolutePath
    }
}