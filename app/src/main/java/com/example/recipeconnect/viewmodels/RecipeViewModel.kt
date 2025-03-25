package com.example.recipeconnect.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.recipeconnect.models.dao.FavoriteRepository
import com.example.recipeconnect.models.dao.RecipeDatabase
import com.example.recipeconnect.models.dao.RecipeRepository
import com.example.recipeconnect.models.Recipe
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val recipeDao = RecipeDatabase.getDatabase(application).recipeDao()
    private val favoriteDao = RecipeDatabase.getDatabase(application).favoriteRecipeDao()

    private val recipeRepository = RecipeRepository(recipeDao)
    private val favoriteRepository = FavoriteRepository(favoriteDao)

    val allRecipes: LiveData<List<Recipe>> = recipeRepository.allRecipes

    fun insert(recipe: Recipe) = viewModelScope.launch {
        recipeRepository.insert(recipe)
    }

    fun delete(recipe: Recipe) = viewModelScope.launch {
        recipeRepository.delete(recipe)
    }

    fun getRecipeById(id: String, callback: (Recipe?) -> Unit) = viewModelScope.launch {
        val recipe = recipeRepository.getRecipeById(id)
        callback(recipe)
    }

    fun deleteAll() = viewModelScope.launch {
        recipeRepository.deleteAll()
    }

    // ⭐ FAVORITES

    fun addToFavorites(recipeId: String, userId: String) = viewModelScope.launch {
        favoriteRepository.addToFavorites(recipeId, userId)
    }

    fun removeFromFavorites(recipeId: String, userId: String) = viewModelScope.launch {
        favoriteRepository.removeFromFavorites(recipeId, userId)
    }

    fun isFavorite(recipeId: String, userId: String, callback: (Boolean) -> Unit) = viewModelScope.launch {
        val result = favoriteRepository.isFavorite(recipeId, userId)
        callback(result)
    }

    fun getFavoriteRecipeIds(userId: String, callback: (List<String>) -> Unit) = viewModelScope.launch {
        val ids = favoriteRepository.getFavoriteRecipeIds(userId)
        callback(ids)
    }
}