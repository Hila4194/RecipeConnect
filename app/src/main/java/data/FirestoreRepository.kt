package com.recipeconnect.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.recipeconnect.model.User

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Fetch the current user's profile
    fun getCurrentUserProfile(callback: (User?, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(null, "User not logged in")
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                callback(user, null)
            }
            .addOnFailureListener { exception ->
                callback(null, exception.message)
            }
    }

    // Follow a chef (this can be expanded further as per your requirements)
    fun followChef(callback: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false, "User not logged in")
            return
        }

        val chefId = "chef_id_here"  // Replace with the actual chef's ID (can be passed as argument)

        val followData = hashMapOf(
            "following" to true,
            "chefId" to chefId
        )

        db.collection("users").document(userId)
            .collection("following").document(chefId)  // Storing followed chefs in a sub-collection
            .set(followData)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message)
            }
    }
}
