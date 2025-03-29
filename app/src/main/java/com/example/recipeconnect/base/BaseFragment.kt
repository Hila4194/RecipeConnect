package com.example.recipeconnect.base

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.recipeconnect.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

abstract class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Enables the toolbar menu
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.logout_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logoutUser()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        Snackbar.make(requireView(), "Logged out", Snackbar.LENGTH_SHORT).show()
        findNavController().navigate(R.id.loginFragment)
    }
}