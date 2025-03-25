package com.example.recipeconnect.models.dao

class FavoriteRepository(private val favoriteDao: FavoriteRecipeDao) {

    suspend fun addToFavorites(recipeId: String, userId: String) {
        val favorite = FavoriteRecipeEntity(recipeId, userId)
        favoriteDao.addToFavorites(favorite)
    }

    suspend fun removeFromFavorites(recipeId: String, userId: String) {
        val favorite = FavoriteRecipeEntity(recipeId, userId)
        favoriteDao.removeFromFavorites(favorite)
    }

    suspend fun getFavoriteRecipeIds(userId: String): List<String> {
        return favoriteDao.getFavoriteRecipeIdsForUser(userId)
    }

    suspend fun isFavorite(recipeId: String, userId: String): Boolean {
        return favoriteDao.isFavorite(userId, recipeId)
    }
}