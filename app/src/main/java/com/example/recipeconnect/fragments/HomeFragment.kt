package com.example.recipeconnect.fragments

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipeconnect.R
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    // Inflate view or redirect if user is already logged in
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()

        return if (auth.currentUser != null) {
            // If user is already authenticated, skip home and go to main screen
            findNavController().navigate(R.id.recipesHomeFragment)
            null
        } else {
            // Otherwise show welcome screen
            inflater.inflate(R.layout.fragment_home, container, false)
        }
    }

    // Handle animations and navigation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Logo fade-in animation on entry
        val logo = view.findViewById<ImageView>(R.id.logoImageView)
        val button = view.findViewById<Button>(R.id.startButton)

        logo.alpha = 0f
        logo.animate().alpha(1f).setDuration(1000).start()

        // Button scaling animation on press (responsive feedback)
        button.setOnTouchListener { v, _ ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                v.animate().scaleX(1f).scaleY(1f).duration = 100
            }
            false
        }

        // Navigate to LoginFragment on button click
        button.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }
}