package com.example.recipeconnect.models.dao

import androidx.lifecycle.LiveData
import com.example.recipeconnect.models.Recipe

class RecipeRepository(private val recipeDao: RecipeDao) {

    val allRecipes: LiveData<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun insert(recipe: Recipe) {
        recipeDao.insertRecipe(recipe)
    }

    suspend fun delete(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe)
    }

    suspend fun getRecipeById(id: String): Recipe? {
        return recipeDao.getRecipeById(id)
    }

    suspend fun deleteAll() {
        recipeDao.deleteAllRecipes()
    }

    suspend fun deleteRecipesByUserId(userId: String) {
        recipeDao.deleteRecipesByUserId(userId)
    }

    suspend fun getAllRecipeUserIds(): List<String> {
        return recipeDao.getAllRecipeUserIds()
    }
}