package com.example.recipeconnect.models.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.models.dao.UserImage
import com.example.recipeconnect.models.dao.FavoriteRecipeEntity

@TypeConverters(Converters::class)
@Database(
    entities = [Recipe::class, FavoriteRecipeEntity::class, UserImage::class],
    version = 2,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun favoriteRecipeDao(): FavoriteRecipeDao
    abstract fun userImageDao(): UserImageDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDatabase? = null

        fun getDatabase(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipe_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}