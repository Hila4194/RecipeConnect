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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.base.BaseFragment
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.util.*

class EditMyRecipeFragment : BaseFragment() {

    private val recipeViewModel: RecipeViewModel by viewModels()
    private val args: EditMyRecipeFragmentArgs by navArgs()

    private lateinit var recipeImageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var recipeTitleEditText: EditText
    private lateinit var prepTimeEditText: EditText
    private lateinit var ingredientsEditText: EditText
    private lateinit var stepsEditText: EditText
    private lateinit var difficultySpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var saveRecipeButton: Button

    private val auth = FirebaseAuth.getInstance()
    private var imageUri: Uri? = null
    private var existingImageUrl: String? = null
    private var originalRecipe: Recipe? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize views
        recipeImageView = view.findViewById(R.id.recipeImageView)
        selectImageButton = view.findViewById(R.id.selectImageButton)
        recipeTitleEditText = view.findViewById(R.id.recipeTitleEditText)
        prepTimeEditText = view.findViewById(R.id.prepTimeEditText)
        ingredientsEditText = view.findViewById(R.id.ingredientsEditText)
        stepsEditText = view.findViewById(R.id.stepsEditText)
        difficultySpinner = view.findViewById(R.id.difficultySpinner)
        categorySpinner = view.findViewById(R.id.categorySpinner)
        saveRecipeButton = view.findViewById(R.id.saveRecipeButton)

        setupSpinners()
        loadRecipe()

        selectImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        saveRecipeButton.setOnClickListener {
            updateRecipe()
        }
    }

    private fun setupSpinners() {
        val difficulties = arrayOf("Easy", "Medium", "Hard")
        val categories = arrayOf("Dairy", "Meat", "Chicken", "Desserts", "Asian", "Italian", "Mexican", "Vegan", "Mediterranean", "Snacks")

        difficultySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, difficulties)
        categorySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
    }

    private fun loadRecipe() {
        val recipeId = args.recipeId
        recipeViewModel.getRecipeById(recipeId) { recipe ->
            recipe?.let {
                originalRecipe = it
                requireActivity().runOnUiThread {
                    recipeTitleEditText.setText(it.title)
                    prepTimeEditText.setText(it.prepTime)
                    ingredientsEditText.setText(it.ingredients.joinToString(", "))
                    stepsEditText.setText(it.steps)
                    difficultySpinner.setSelection((difficultySpinner.adapter as ArrayAdapter<String>).getPosition(it.difficulty))
                    categorySpinner.setSelection((categorySpinner.adapter as ArrayAdapter<String>).getPosition(it.category))

                    existingImageUrl = it.imageUrl
                    Glide.with(requireContext())
                        .load(existingImageUrl)
                        .placeholder(R.drawable.default_recipe)
                        .into(recipeImageView)
                }
            }
        }
    }

    private fun updateRecipe() {
        val scrollView = requireView().findViewById<ScrollView>(R.id.editRecipeScrollView)
        val progressBar = requireView().findViewById<ProgressBar>(R.id.editProgressBar)

        val title = recipeTitleEditText.text.toString().trim()
        val prepTime = prepTimeEditText.text.toString().trim()
        val ingredients = ingredientsEditText.text.toString().split(",").map { it.trim() }
        val steps = stepsEditText.text.toString().trim()
        val difficulty = difficultySpinner.selectedItem.toString()
        val category = categorySpinner.selectedItem.toString()

        if (title.isEmpty() || prepTime.isEmpty() || steps.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        scrollView.alpha = 0.5f

        val imageUrl = if (imageUri != null) {
            saveImageToInternalStorage(imageUri!!, UUID.randomUUID().toString())
        } else if (!existingImageUrl.isNullOrBlank() && File(existingImageUrl!!).exists()) {
            existingImageUrl!!
        } else ""

        val updatedRecipe = Recipe(
            id = originalRecipe!!.id,
            title = title,
            ingredients = ingredients,
            steps = steps,
            imageUrl = imageUrl,
            userId = originalRecipe!!.userId,
            prepTime = prepTime,
            difficulty = difficulty,
            category = category
        )

        recipeViewModel.insert(updatedRecipe)

        progressBar.visibility = View.GONE
        scrollView.alpha = 1f

        Toast.makeText(requireContext(), "Recipe updated!", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            recipeImageView.setImageURI(imageUri)
        }
    }

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