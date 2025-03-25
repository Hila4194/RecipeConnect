package com.example.recipeconnect.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class AddRecipeActivity : AppCompatActivity() {

    private lateinit var recipeImageView: ImageView
    private lateinit var recipeTitleEditText: EditText
    private lateinit var prepTimeEditText: EditText
    private lateinit var difficultySpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var ingredientsEditText: EditText
    private lateinit var stepsEditText: EditText
    private lateinit var saveRecipeButton: Button
    private var imageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance()
    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        recipeImageView = findViewById(R.id.recipeImageView)
        recipeTitleEditText = findViewById(R.id.recipeTitleEditText)
        prepTimeEditText = findViewById(R.id.prepTimeEditText)
        difficultySpinner = findViewById(R.id.difficultySpinner)
        categorySpinner = findViewById(R.id.categorySpinner)
        ingredientsEditText = findViewById(R.id.ingredientsEditText)
        stepsEditText = findViewById(R.id.stepsEditText)
        saveRecipeButton = findViewById(R.id.saveRecipeButton)

        setupSpinners()

        findViewById<Button>(R.id.selectImageButton).setOnClickListener {
            selectImageFromGallery()
        }

        saveRecipeButton.setOnClickListener {
            validateAndSaveRecipe()
        }
    }

    private fun setupSpinners() {
        difficultySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Easy", "Medium", "Hard"))
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Dairy", "Meat", "Chicken", "Desserts", "Asian"))
    }

    private fun validateAndSaveRecipe() {
        val title = recipeTitleEditText.text.toString().trim()
        val prepTime = prepTimeEditText.text.toString().trim()
        val difficulty = difficultySpinner.selectedItem.toString()
        val category = categorySpinner.selectedItem.toString()
        val ingredientsText = ingredientsEditText.text.toString().trim()
        val ingredients = ingredientsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val steps = stepsEditText.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        if (title.isEmpty() || prepTime.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val recipe = Recipe(
            id = UUID.randomUUID().toString(),
            title = title,
            prepTime = prepTime,
            difficulty = difficulty,
            category = category,
            ingredients = ingredients,
            steps = steps,
            imageUrl = imageUri?.toString() ?: "",
            userId = userId
        )

        recipeViewModel.insert(recipe)

        Toast.makeText(this, "Recipe saved locally!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            Glide.with(this).load(imageUri).into(recipeImageView)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}