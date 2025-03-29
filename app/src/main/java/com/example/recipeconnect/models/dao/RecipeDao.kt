package com.example.recipeconnect.models.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.recipeconnect.models.Recipe

@Dao
interface RecipeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<Recipe>)

    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: String): Recipe?

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()

    @Query("DELETE FROM recipes WHERE userId = :userId")
    suspend fun deleteRecipesByUserId(userId: String)

    @Query("SELECT DISTINCT userId FROM recipes")
    suspend fun getAllRecipeUserIds(): List<String>
}