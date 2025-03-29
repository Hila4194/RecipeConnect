package com.example.recipeconnect.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.recipeconnect.R
import com.example.recipeconnect.base.BaseFragment
import com.example.recipeconnect.models.User
import com.example.recipeconnect.models.dao.RecipeDatabase
import com.example.recipeconnect.utils.CircleTransform
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.File

class UserProfileFragment : BaseFragment() {

    // UI Components
    private lateinit var profileImageView: ImageView
    private lateinit var firstNameTextView: EditText
    private lateinit var lastNameTextView: EditText
    private lateinit var dobTextView: EditText
    private lateinit var emailTextView: EditText
    private lateinit var bioTextView: EditText
    private lateinit var editProfileButton: Button
    private lateinit var myRecipesButton: Button
    private lateinit var favoriteRecipesButton: Button

    // Firebase instances
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup toolbar with back navigation
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Bind views
        profileImageView = view.findViewById(R.id.profileImageView)
        firstNameTextView = view.findViewById(R.id.firstNameTextView)
        lastNameTextView = view.findViewById(R.id.lastNameTextView)
        dobTextView = view.findViewById(R.id.dobTextView)
        emailTextView = view.findViewById(R.id.emailEditText)
        bioTextView = view.findViewById(R.id.bioTextView)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        myRecipesButton = view.findViewById(R.id.myRecipesButton)
        favoriteRecipesButton = view.findViewById(R.id.favoriteRecipesButton)

        // Load user profile info and image
        loadUserProfile()

        // Navigate to edit profile
        editProfileButton.setOnClickListener {
            val action = UserProfileFragmentDirections.actionUserProfileFragmentToEditProfileFragment()
            findNavController().navigate(action)
        }

        // Navigate to My Recipes screen
        myRecipesButton.setOnClickListener {
            val action = UserProfileFragmentDirections.actionUserProfileFragmentToMyRecipesFragment()
            findNavController().navigate(action)
        }

        // Navigate to Favorite Recipes screen
        favoriteRecipesButton.setOnClickListener {
            val action = UserProfileFragmentDirections.actionUserProfileFragmentToFavoriteRecipesFragment()
            findNavController().navigate(action)
        }
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        // Fetch profile info from Firestore
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
                Snackbar.make(requireView(), "Failed to load profile", Snackbar.LENGTH_SHORT)
                    .setAnchorView(editProfileButton)
                    .show()
            }

        // Load local profile image from ROOM (Room -> DAO -> internal file)
        lifecycleScope.launch {
            val db = RecipeDatabase.getDatabase(requireContext())
            val userImage = db.userImageDao().get(uid)
            val imageFile = userImage?.imagePath?.let { File(it) }

            if (imageFile != null && imageFile.exists()) {
                Picasso.get()
                    .load(imageFile)
                    .transform(CircleTransform())
                    .memoryPolicy(
                        com.squareup.picasso.MemoryPolicy.NO_CACHE,
                        com.squareup.picasso.MemoryPolicy.NO_STORE
                    )
                    .into(profileImageView)
            } else {
                Picasso.get()
                    .load(R.drawable.default_profile_image)
                    .transform(CircleTransform())
                    .into(profileImageView)
            }
        }
    }

    // Ensure profile is always fresh when returning from Edit
    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }
}