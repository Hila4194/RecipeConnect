package com.example.recipeconnect.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.viewmodels.RecipeViewModel
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

class EditMyRecipeActivity : AppCompatActivity() {

    private val recipeViewModel: RecipeViewModel by viewModels()

    private lateinit var recipeImageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var recipeTitleEditText: EditText
    private lateinit var prepTimeEditText: EditText
    private lateinit var ingredientsEditText: EditText
    private lateinit var stepsEditText: EditText
    private lateinit var difficultySpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var saveRecipeButton: Button

    private var imageUri: Uri? = null
    private var recipeId: String? = null
    private var existingImageUrl: String? = null
    private var originalRecipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Init views
        recipeImageView = findViewById(R.id.recipeImageView)
        selectImageButton = findViewById(R.id.selectImageButton)
        recipeTitleEditText = findViewById(R.id.recipeTitleEditText)
        prepTimeEditText = findViewById(R.id.prepTimeEditText)
        ingredientsEditText = findViewById(R.id.ingredientsEditText)
        stepsEditText = findViewById(R.id.stepsEditText)
        difficultySpinner = findViewById(R.id.difficultySpinner)
        categorySpinner = findViewById(R.id.categorySpinner)
        saveRecipeButton = findViewById(R.id.saveRecipeButton)

        recipeId = intent.getStringExtra("RECIPE_ID")

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
        val categories = arrayOf("Dairy", "Meat", "Chicken", "Desserts", "Asian")

        difficultySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, difficulties)
        categorySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
    }

    private fun loadRecipe() {
        recipeId?.let { id ->
            recipeViewModel.getRecipeById(id) { recipe ->
                recipe?.let {
                    originalRecipe = it
                    runOnUiThread {
                        recipeTitleEditText.setText(it.title)
                        prepTimeEditText.setText(it.prepTime)
                        ingredientsEditText.setText(it.ingredients.joinToString(", "))
                        stepsEditText.setText(it.steps)
                        difficultySpinner.setSelection((difficultySpinner.adapter as ArrayAdapter<String>).getPosition(it.difficulty))
                        categorySpinner.setSelection((categorySpinner.adapter as ArrayAdapter<String>).getPosition(it.category))

                        existingImageUrl = it.imageUrl
                        Glide.with(this)
                            .load(existingImageUrl)
                            .placeholder(R.drawable.default_recipe)
                            .into(recipeImageView)
                    }
                }
            }
        }
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

    private fun updateRecipe() {
        val title = recipeTitleEditText.text.toString().trim()
        val prepTime = prepTimeEditText.text.toString().trim()
        val ingredients = ingredientsEditText.text.toString().split(",").map { it.trim() }
        val steps = stepsEditText.text.toString().trim()
        val difficulty = difficultySpinner.selectedItem.toString()
        val category = categorySpinner.selectedItem.toString()

        if (title.isEmpty() || prepTime.isEmpty() || steps.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val imageUrl: String = if (imageUri != null) {
            saveImageToInternalStorage(imageUri!!, UUID.randomUUID().toString())
        } else if (!existingImageUrl.isNullOrBlank() && File(existingImageUrl!!).exists()) {
            existingImageUrl!!
        } else {
            ""
        }

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

        Toast.makeText(this, "Recipe updated!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun saveImageToInternalStorage(uri: Uri, fileName: String): String {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(filesDir, "$fileName.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
        return file.absolutePath
    }
}