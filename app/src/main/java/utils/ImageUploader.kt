package com.recipeconnect.utils

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

object ImageUploader {
    private val storageRef = FirebaseStorage.getInstance().reference

    fun uploadImage(uri: Uri, folder: String, callback: (String?) -> Unit) {
        val imageRef = storageRef.child("$folder/${uri.lastPathSegment}")
        imageRef.putFile(uri).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }
        }.addOnFailureListener {
            callback(null)
        }
    }
}
