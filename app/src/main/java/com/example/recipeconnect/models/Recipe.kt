package com.example.recipeconnect.models

data class Recipe(
    var id: String = "",  // Firestore document ID
    var title: String = "",
    var ingredients: List<String> = emptyList(),
    var steps: String = "",
    var imageUrl: String = "",
    var userId: String = "", // The ID of the user who created the recipe
    var prepTime: String = "", // Example: "30 minutes"
    var difficulty: String = "", // Options: "Easy", "Medium", "Hard"
    var category: String = "" // Options: "Dairy", "Meat", "Chicken", "Desserts", "Asian"
)