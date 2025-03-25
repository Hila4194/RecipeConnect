package com.example.recipeconnect.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.recipeconnect.R
import com.example.recipeconnect.models.User
import com.example.recipeconnect.models.dao.RecipeDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.example.recipeconnect.utils.CircleTransform
import kotlinx.coroutines.launch
import java.io.File

class UserProfileActivity : AppCompatActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var firstNameTextView: EditText
    private lateinit var lastNameTextView: EditText
    private lateinit var dobTextView: EditText
    private lateinit var emailTextView: EditText
    private lateinit var bioTextView: EditText
    private lateinit var editProfileButton: Button
    private lateinit var myRecipesButton: Button
    private lateinit var favoriteRecipesButton: Button

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        profileImageView = findViewById(R.id.profileImageView)
        firstNameTextView = findViewById(R.id.firstNameTextView)
        lastNameTextView = findViewById(R.id.lastNameTextView)
        dobTextView = findViewById(R.id.dobTextView)
        emailTextView = findViewById(R.id.emailEditText)
        bioTextView = findViewById(R.id.bioTextView)
        editProfileButton = findViewById(R.id.editProfileButton)
        myRecipesButton = findViewById(R.id.myRecipesButton)
        favoriteRecipesButton = findViewById(R.id.favoriteRecipesButton)

        loadUserProfile()

        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        myRecipesButton.setOnClickListener {
            startActivity(Intent(this, MyRecipesActivity::class.java))
        }

        favoriteRecipesButton.setOnClickListener {
            startActivity(Intent(this, FavoriteRecipesActivity::class.java))
        }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        // Load basic user data from Firestore
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    firstNameTextView.setText(user?.firstName ?: "N/A")
                    lastNameTextView.setText(user?.lastName ?: "N/A")
                    dobTextView.setText(user?.dob ?: "N/A")
                    emailTextView.setText(user?.email ?: "N/A")
                    bioTextView.setText(user?.bio ?: "No bio available")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }

        // Load profile image from Room
        lifecycleScope.launch {
            val db = RecipeDatabase.getDatabase(applicationContext)
            val userImage = db.userImageDao().get(uid)
            userImage?.let {
                val imageFile = File(it.imagePath)
                if (imageFile.exists()) {
                    Picasso.get()
                        .load(imageFile)
                        .transform(CircleTransform())
                        .into(profileImageView)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.recipe_home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.menu_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile() // Reload fresh data every time the screen resumes
    }
}