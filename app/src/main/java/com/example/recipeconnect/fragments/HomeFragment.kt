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

        // Animate logo fade-in
        val logo = view.findViewById<ImageView>(R.id.logoImageView)
        val button = view.findViewById<Button>(R.id.startButton)

        logo.alpha = 0f
        logo.animate().alpha(1f).setDuration(1000).start()

        // Button press animation
        button.setOnTouchListener { v, _ ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                v.animate().scaleX(1f).scaleY(1f).duration = 100
            }
            false
        }

        button.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }
}