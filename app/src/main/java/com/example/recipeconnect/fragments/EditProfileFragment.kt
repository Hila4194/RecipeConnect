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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment : BaseFragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button
    private var imageUri: Uri? = null

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        profileImageView = view.findViewById(R.id.editProfileImageView)
        firstNameEditText = view.findViewById(R.id.editFirstName)
        lastNameEditText = view.findViewById(R.id.editLastName)
        bioEditText = view.findViewById(R.id.editBio)
        saveButton = view.findViewById(R.id.saveProfileButton)
        val changeImageButton = view.findViewById<Button>(R.id.changeProfileImageButton)

        changeImageButton.setOnClickListener {
            selectImageFromGallery()
        }

        saveButton.setOnClickListener {
            saveUserProfile()
        }
    }

    override fun onResume() {
        super.onResume()
        // Only reload profile if no image is being previewed
        if (imageUri == null) {
            loadUserProfile()
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
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
            }

        lifecycleScope.launch {
            val db = RecipeDatabase.getDatabase(requireContext())
            val userImage = db.userImageDao().get(uid)
            val file = userImage?.imagePath?.let { File(it) }
            if (file != null && file.exists()) {
                Picasso.get()
                    .load(file)
                    .transform(CircleTransform())
                    .memoryPolicy(com.squareup.picasso.MemoryPolicy.NO_CACHE, com.squareup.picasso.MemoryPolicy.NO_STORE)
                    .into(profileImageView)
            } else {
                Picasso.get()
                    .load(R.drawable.default_profile_image)
                    .transform(CircleTransform())
                    .into(profileImageView)
            }
        }
    }

    private fun saveUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()

        val scrollView = requireView().findViewById<ScrollView>(R.id.editProfileScrollView)
        val progressBar = requireView().findViewById<ProgressBar>(R.id.editProfileProgressBar)

        progressBar.visibility = View.VISIBLE
        scrollView.alpha = 0.5f

        lifecycleScope.launch {
            val db = RecipeDatabase.getDatabase(requireContext())

            if (imageUri != null) {
                val imagePath = saveImageToInternalStorage(imageUri!!, "profile_$uid")
                val userImage = UserImage(uid = uid, imagePath = imagePath)
                db.userImageDao().insert(userImage)
            }

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
                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    scrollView.alpha = 1f
                    Toast.makeText(requireContext(), "Error saving profile", Toast.LENGTH_SHORT).show()
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
            imageUri?.let {
                profileImageView.setImageURI(it)
            }
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