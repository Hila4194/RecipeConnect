package com.example.recipeconnect.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.recipeconnect.R
import com.example.recipeconnect.base.BaseFragment
import com.example.recipeconnect.models.dao.RecipeDatabase
import com.example.recipeconnect.models.dao.UserImage
import com.example.recipeconnect.utils.CircleTransform
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment : BaseFragment() {

    // UI elements
    private lateinit var profileImageView: ImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button
    private var imageUri: Uri? = null

    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Inflate fragment layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_edit_profile, container, false)

    // Setup UI and actions
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup toolbar with back navigation
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Bind UI elements
        profileImageView = view.findViewById(R.id.editProfileImageView)
        firstNameEditText = view.findViewById(R.id.editFirstName)
        lastNameEditText = view.findViewById(R.id.editLastName)
        bioEditText = view.findViewById(R.id.editBio)
        saveButton = view.findViewById(R.id.saveProfileButton)
        val changeImageButton = view.findViewById<Button>(R.id.changeProfileImageButton)

        // Change profile picture
        changeImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        // Save profile changes
        saveButton.setOnClickListener {
            saveUserProfile()
        }
    }

    // Reload profile picture if it hasn't been changed
    override fun onResume() {
        super.onResume()
        if (imageUri == null) {
            loadUserProfile()
        }
    }

    // Loads user data from Firestore + profile image from local Room DB
    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        // Fetch user text data (firstName, lastName, bio)
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    firstNameEditText.setText(document.getString("firstName") ?: "")
                    lastNameEditText.setText(document.getString("lastName") ?: "")
                    bioEditText.setText(document.getString("bio") ?: "")
                }
            }
            .addOnFailureListener {
                Snackbar.make(requireView(), "Failed to load profile", Snackbar.LENGTH_SHORT).show()
            }

        // Load user profile image from internal storage using Room
        lifecycleScope.launch {
            val db = RecipeDatabase.getDatabase(requireContext())
            val userImage = db.userImageDao().get(uid)
            val file = userImage?.imagePath?.let { File(it) }
            if (file != null && file.exists()) {
                Picasso.get()
                    .load(file)
                    .transform(CircleTransform()) // Round image
                    .memoryPolicy(
                        com.squareup.picasso.MemoryPolicy.NO_CACHE,
                        com.squareup.picasso.MemoryPolicy.NO_STORE
                    )
                    .into(profileImageView)
            } else {
                // Default profile image
                Picasso.get()
                    .load(R.drawable.default_profile_image)
                    .transform(CircleTransform())
                    .into(profileImageView)
            }
        }
    }

    // Saves the profile data and image locally & remotely
    private fun saveUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()

        // Validate inputs
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Snackbar.make(requireView(), "First and Last name are required", Snackbar.LENGTH_SHORT).show()
            return
        }

        val scrollView = requireView().findViewById<ScrollView>(R.id.editProfileScrollView)
        val progressBar = requireView().findViewById<ProgressBar>(R.id.editProfileProgressBar)

        // Show loading state
        progressBar.visibility = View.VISIBLE
        scrollView.alpha = 0.5f

        lifecycleScope.launch {
            val db = RecipeDatabase.getDatabase(requireContext())

            // Save new image locally if changed
            if (imageUri != null) {
                val imagePath = saveImageToInternalStorage(imageUri!!, "profile_$uid")
                val userImage = UserImage(uid = uid, imagePath = imagePath)
                db.userImageDao().insert(userImage)
            }

            // Save user text data to Firestore
            val userData = mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "bio" to bio
            )

            firestore.collection("users").document(uid)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    scrollView.alpha = 1f
                    Snackbar.make(requireView(), "Profile updated!", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    scrollView.alpha = 1f
                    Snackbar.make(requireView(), "Error saving profile", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    // Opens gallery to pick a new profile image
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, 100)
    }

    // Handles selected image from gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageUri?.let {
                profileImageView.setImageURI(it)
            }
        }
    }

    // Saves selected image to app's internal storage, returns full file path
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