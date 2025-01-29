package com.recipeconnect.model

data class User(
    val id: String = "",
    val name: String = "",
    val bio: String = "",
    val imageUrl: String = "",
    val favoriteCuisines: List<String> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList(),
    val following: List<String> = emptyList() // List of followed chefs' user IDs
)

