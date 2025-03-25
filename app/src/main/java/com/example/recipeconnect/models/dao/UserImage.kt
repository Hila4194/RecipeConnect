package com.example.recipeconnect.models.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_images")
data class UserImage(
    @PrimaryKey val uid: String,
    val imagePath: String // local path to profile image
)