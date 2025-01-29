package com.recipeconnect.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.recipeconnect.model.User

class FirestoreRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun registerUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = User(auth.currentUser?.uid ?: "", email)
                    db.collection("users").document(user.id).set(user)
                        .addOnSuccessListener { callback(true, null) }
                        .addOnFailureListener { callback(false, it.message) }
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful, task.exception?.message)
            }
    }
}
