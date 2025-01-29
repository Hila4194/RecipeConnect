package com.recipeconnect.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.recipeconnect.data.FirestoreRepository
import com.recipeconnect.model.User

class ProfileViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val _userProfile = MutableLiveData<User>()
    val userProfile: LiveData<User> get() = _userProfile

    init {
        fetchUserProfile()
    }

    // Fetch the current user profile
    fun fetchUserProfile() {
        repository.getCurrentUserProfile { user, error ->
            if (error == null) {
                _userProfile.value = user
            }
        }
    }

    // Follow a chef (this can be expanded based on the data structure)
    fun followChef(callback: (Boolean, String?) -> Unit) {
        repository.followChef { success, message ->
            callback(success, message)
        }
    }
}
