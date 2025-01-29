package com.recipeconnect.model

data class `Recipe.kt`(
    val recipeId: String = "",
    val title: String = "",
    val ingredients: List<String> = listOf(),
    val instructions: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val cookingTime: Int = 0,
    val difficulty: String = "",
    val userId: String = ""
)
