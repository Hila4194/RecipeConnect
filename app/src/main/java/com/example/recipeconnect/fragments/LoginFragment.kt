package com.example.recipeconnect.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipeconnect.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    // Inflate the login layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    // Bind views, handle login logic and navigation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // UI element references
        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val signupButton = view.findViewById<Button>(R.id.signupButton)
        val progressBar = view.findViewById<ProgressBar>(R.id.loginProgressBar)
        val loginForm = view.findViewById<View>(R.id.loginCard)

        // Login button clicked
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Clear previous errors
            emailEditText.error = null
            passwordEditText.error = null

            // Input validation
            if (email.isEmpty()) {
                emailEditText.error = "Email is required"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Enter a valid email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordEditText.error = "Password is required"
                return@setOnClickListener
            }
            if (password.length < 6) {
                passwordEditText.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            // Show loading state
            progressBar.visibility = View.VISIBLE
            loginForm.alpha = 0.5f

            // Attempt Firebase login
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // Success: hide loader and navigate to home
                    progressBar.visibility = View.GONE
                    loginForm.alpha = 1f
                    Snackbar.make(requireView(), "Login successful!", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_recipesHomeFragment)
                }
                .addOnFailureListener {
                    // Failure: show error message
                    progressBar.visibility = View.GONE
                    loginForm.alpha = 1f
                    Snackbar.make(
                        requireView(),
                        "Login failed: ${it.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
        }

        // Navigate to signup screen
        signupButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }
}