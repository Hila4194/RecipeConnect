package com.example.recipeconnect.fragments

import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipeconnect.R
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()

        return if (auth.currentUser != null) {
            findNavController().navigate(R.id.recipesHomeFragment)
            null
        } else {
            inflater.inflate(R.layout.fragment_home, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoImage = view.findViewById<ImageView>(R.id.logoImageView)
        val startButton = view.findViewById<Button>(R.id.startButton)

        // Logo fade-in animation
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        logoImage.startAnimation(fadeIn)

        // Button pulse animation
        val pulse = AnimationUtils.loadAnimation(requireContext(), R.anim.scale)
        startButton.setOnTouchListener { v, event ->
            v.startAnimation(pulse)
            false
        }

        // Navigation
        startButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }
}