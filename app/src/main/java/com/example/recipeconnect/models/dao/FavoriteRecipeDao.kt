package com.example.recipeconnect.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface FavoriteRecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFavorites(favorite: FavoriteRecipeEntity)

    @Delete
    suspend fun removeFromFavorites(favorite: FavoriteRecipeEntity)

    @Query("SELECT recipeId FROM favorite_recipes WHERE userId = :userId")
    suspend fun getFavoriteRecipeIdsForUser(userId: String): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE userId = :userId AND recipeId = :recipeId)")
    suspend fun isFavorite(userId: String, recipeId: String): Boolean
}