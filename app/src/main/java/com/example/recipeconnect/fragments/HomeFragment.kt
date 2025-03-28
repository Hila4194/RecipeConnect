package com.example.recipeconnect.fragments

import android.os.Bundle
import android.view.*
import android.widget.Button
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
            // ✅ If already logged in, go directly to RecipesHomeFragment
            findNavController().navigate(R.id.recipesHomeFragment)
            null // No need to display the home screen
        } else {
            inflater.inflate(R.layout.fragment_home, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startButton = view.findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            // Navigate to LoginFragment
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }
}