package com.example.recipeconnect.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

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
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_recipe)

        // Setup custom centered toolbar
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
        val ingredients = ingredientsText.split(",").map { it.trim() } // Split input by commas and create a list
        val steps = stepsEditText.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        if (title.isEmpty() || prepTime.isEmpty() || ingredients.isEmpty() || steps.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Debugging: Check if all inputs are valid
        Toast.makeText(this, "Saving recipe: $title", Toast.LENGTH_SHORT).show()

        if (imageUri != null) {
            uploadImageAndSaveRecipe(userId, title, prepTime, difficulty, category, ingredients, steps)
        } else {
            saveRecipeToFirestore(userId, title, prepTime, difficulty, category, ingredients, steps, null)
        }
    }

    private fun uploadImageAndSaveRecipe(
        userId: String,
        title: String,
        prepTime: String,
        difficulty: String,
        category: String,
        ingredients: List<String>,
        steps: String
    ) {
        val imageRef = storage.reference.child("recipe_images/${UUID.randomUUID()}.jpg")

        imageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveRecipeToFirestore(userId, title, prepTime, difficulty, category, ingredients, steps, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveRecipeToFirestore(
        userId: String,
        title: String,
        prepTime: String,
        difficulty: String,
        category: String,
        ingredients: List<String>,
        steps: String,
        imageUrl: String?
    ) {
        val recipe = Recipe(
            title = title,
            prepTime = prepTime,
            difficulty = difficulty,
            category = category,
            ingredients = ingredients,
            steps = steps,
            imageUrl = imageUrl ?: "",
            userId = userId
        )

        firestore.collection("recipes").add(recipe)
            .addOnSuccessListener {
                Toast.makeText(this, "Recipe added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save recipe", Toast.LENGTH_SHORT).show()
            }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
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