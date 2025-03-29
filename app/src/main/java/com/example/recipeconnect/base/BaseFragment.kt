package com.example.recipeconnect.base

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipeconnect.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

// Abstract base class for fragments that require logout functionality
abstract class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Enables options menu in this fragment
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.logout_menu, menu) // Inflate the logout menu (defined in res/menu/logout_menu.xml)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutUser() // Trigger logout logic
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Signs out the current user and navigates back to the login screen
    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut() // Firebase logout
        Snackbar.make(requireView(), "Logged out", Snackbar.LENGTH_SHORT).show() // Show confirmation message
        findNavController().navigate(R.id.loginFragment) // Navigate to login fragment
    }
}