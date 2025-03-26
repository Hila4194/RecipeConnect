package com.example.recipeconnect.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.recipeconnect.R
import com.example.recipeconnect.models.dao.RecipeDatabase
import com.example.recipeconnect.models.dao.UserImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import com.example.recipeconnect.utils.CircleTransform

class EditProfileActivity : AppCompatActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button
    private var imageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Centered custom title
        val titleTextView = findViewById<TextView>(R.id.toolbarTitle)
        titleTextView?.text = "Edit Profile"

        profileImageView = findViewById(R.id.editProfileImageView)
        firstNameEditText = findViewById(R.id.editFirstName)
        lastNameEditText = findViewById(R.id.editLastName)
        bioEditText = findViewById(R.id.editBio)
        saveButton = findViewById(R.id.saveProfileButton)
        val changeProfileImageButton: Button = findViewById(R.id.changeProfileImageButton)

        loadUserProfile()

        changeProfileImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        saveButton.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    firstNameEditText.setText(document.getString("firstName") ?: "")
                    lastNameEditText.setText(document.getString("lastName") ?: "")
                    bioEditText.setText(document.getString("bio") ?: "")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }

        lifecycleScope.launch {
            val db = RecipeDatabase.getDatabase(applicationContext)
            val userImage = db.userImageDao().get(uid)
            val file = userImage?.imagePath?.let { File(it) }
            if (file != null && file.exists()) {
                Picasso.get()
                    .load(file)
                    .transform(CircleTransform())
                    .into(profileImageView)
            } else {
                Picasso.get()
                    .load(R.drawable.default_profile_image)
                    .transform(CircleTransform())
                    .into(profileImageView)
            }
        }
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
            profileImageView.setImageURI(imageUri)
        }
    }

    private fun saveUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()

        if (imageUri != null) {
            val imagePath = saveImageToInternalStorage(imageUri!!, "profile_$uid")
            val userImage = UserImage(uid = uid, imagePath = imagePath)

            lifecycleScope.launch {
                val db = RecipeDatabase.getDatabase(applicationContext)
                db.userImageDao().insert(userImage)
            }
        }

        val userData = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "bio" to bio
        )

        firestore.collection("users").document(uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()

                // ✅ Return to previous screen and trigger refresh
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving profile", Toast.LENGTH_SHORT).show()
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
        menu?.add(Menu.NONE, R.id.menu_logout, Menu.NONE, "Logout")
            ?.setIcon(R.drawable.ic_logout)
            ?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
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
}