package com.recipeconnect.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.recipeconnect.databinding.FragmentProfileBinding
import com.recipeconnect.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Initialize the ViewModel
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Observe the user profile data
        profileViewModel.userProfile.observe(viewLifecycleOwner) { user ->
            binding.tvUserName.text = user?.name ?: "Guest"
            // Other UI elements like user bio, followers count can be added here
        }

        // Fetch the user profile when the fragment is loaded
        profileViewModel.fetchUserProfile()

        // Follow button functionality
        binding.btnFollow.setOnClickListener {
            profileViewModel.followChef { success, message ->
                if (success) {
                    Toast.makeText(requireContext(), "Followed successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), message ?: "Failed to follow", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }
}
