package com.example.recipeconnect.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.models.User
import com.example.recipeconnect.models.dao.RecipeDatabase
import com.example.recipeconnect.models.dao.UserImage
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var imageUri: Uri? = null

    private lateinit var profileImageView: ShapeableImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val backButton = view.findViewById<View>(R.id.backButton)
        val firstNameEditText = view.findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = view.findViewById<EditText>(R.id.lastNameEditText)
        val dobEditText = view.findViewById<EditText>(R.id.dobEditText)
        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val bioEditText = view.findViewById<EditText>(R.id.bioEditText)
        val changeProfileImageButton = view.findViewById<MaterialButton>(R.id.changeProfileImageButton)
        val createAccountButton = view.findViewById<MaterialButton>(R.id.createAccountButton)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val scrollView = view.findViewById<ScrollView>(R.id.signupScrollView)

        profileImageView = view.findViewById(R.id.profileImageView)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        changeProfileImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, 100)
        }

        dobEditText.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of Birth")
                .build()

            datePicker.show(parentFragmentManager, "DOB_PICKER")

            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateString = sdf.format(Date(selection))
                dobEditText.setText(dateString)
            }
        }

        createAccountButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString().trim()
            val lastName = lastNameEditText.text.toString().trim()
            val dob = dobEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val bio = bioEditText.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Snackbar.make(view, "Please fill in all fields", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            scrollView.alpha = 0.5f

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid != null) {
                        saveProfileImageLocallyAndContinue(uid, firstName, lastName, dob, email, bio, progressBar, scrollView)
                    }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    scrollView.alpha = 1f
                    Snackbar.make(view, "Signup failed: ${it.message}", Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private fun saveProfileImageLocallyAndContinue(
        uid: String,
        firstName: String,
        lastName: String,
        dob: String,
        email: String,
        bio: String,
        progressBar: ProgressBar,
        scrollView: ScrollView
    ) {
        if (imageUri != null) {
            val imagePath = saveImageToInternalStorage(imageUri!!, "profile_$uid")
            val userImage = UserImage(uid = uid, imagePath = imagePath)

            lifecycleScope.launch {
                val db = RecipeDatabase.getDatabase(requireContext())
                db.userImageDao().insert(userImage)
                saveUserToFirestore(uid, firstName, lastName, dob, email, bio, null, progressBar, scrollView)
            }
        } else {
            saveUserToFirestore(uid, firstName, lastName, dob, email, bio, null, progressBar, scrollView)
        }
    }

    private fun saveUserToFirestore(
        uid: String,
        firstName: String,
        lastName: String,
        dob: String,
        email: String,
        bio: String,
        imageUrl: String?,
        progressBar: ProgressBar,
        scrollView: ScrollView
    ) {
        val user = User(
            uid = uid,
            firstName = firstName,
            lastName = lastName,
            dob = dob,
            email = email,
            bio = bio,
            profileImageUrl = imageUrl ?: ""
        )

        firestore.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                scrollView.alpha = 1f
                Snackbar.make(requireView(), "Account created successfully!", Snackbar.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_signupFragment_to_recipesHomeFragment)
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                scrollView.alpha = 1f
                Snackbar.make(requireView(), "Error saving user", Snackbar.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            Glide.with(this).load(imageUri).into(profileImageView)
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