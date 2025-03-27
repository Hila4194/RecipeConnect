package com.example.recipeconnect.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.viewmodels.RecipeViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
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

        // ✅ Save image to internal storage
        val imageUrl = if (imageUri != null) {
            saveImageToInternalStorage(imageUri!!, UUID.randomUUID().toString())
        } else {
            ""
        }

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

        Toast.makeText(this, "Recipe saved!", Toast.LENGTH_SHORT).show()
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

    private fun saveImageToInternalStorage(uri: Uri, fileName: String): String {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(filesDir, "$fileName.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
        return file.absolutePath
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.logout_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java) // replace with your login screen
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}