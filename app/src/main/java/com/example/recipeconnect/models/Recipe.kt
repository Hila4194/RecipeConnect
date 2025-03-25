package com.example.recipeconnect.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String = "",  // Unique ID for Room
    var title: String = "",
    var ingredients: List<String> = emptyList(),
    var steps: String = "",
    var imageUrl: String = "",
    var userId: String = "",
    var prepTime: String = "",
    var difficulty: String = "",
    var category: String = ""
)