package com.example.recipeconnect.models

data class User(
    val uid: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val dob: String = "", // Date of Birth as a string (DD/MM/YYYY format)
    val email: String = "",
    val bio: String = "",
    val profileImageUrl: String = ""
)