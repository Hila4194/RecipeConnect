package com.example.recipeconnect.adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.recipeconnect.R
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.models.dao.RecipeDatabase
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

class RecipeAdapter(
    private var recipes: List<Recipe>,                      // List of recipes to display
    private val emailMap: Map<String, String>,              // Mapping userId -> user email
    private val context: Context,                           // Required for Glide and DAO
    private val onItemClick: (Recipe) -> Unit               // Callback when a recipe item is clicked
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private val userImageDao = RecipeDatabase.getDatabase(context).userImageDao()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position]) // Bind data to view holder
    }

    override fun getItemCount() = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged() // Refresh RecyclerView
    }

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = view.findViewById(R.id.recipeTitleTextView)
        private val emailTextView: TextView = view.findViewById(R.id.emailTextView)
        private val profileImageView: ImageView = view.findViewById(R.id.profileImageView)
        private val recipeImageView: ImageView = view.findViewById(R.id.recipeImageView)

        fun bind(recipe: Recipe) {
            titleTextView.text = recipe.title
            emailTextView.text = emailMap[recipe.userId] ?: "Unknown"

            val imagePath = recipe.imageUrl
            Log.d("RecipeAdapter", "Image path: $imagePath")

            // Handle image source: content URI vs local file path
            if (!imagePath.isNullOrEmpty()) {
                if (imagePath.startsWith("content://")) {
                    // If image is from gallery (content URI), copy it to a cache file before loading with Glide
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val uri = Uri.parse(imagePath)
                            val fileName = UUID.randomUUID().toString() + ".jpg"
                            val destFile = File(context.cacheDir, fileName)

                            context.contentResolver.openInputStream(uri)?.use { input ->
                                FileOutputStream(destFile).use { output ->
                                    input.copyTo(output)
                                }
                            }

                            withContext(Dispatchers.Main) {
                                Glide.with(context)
                                    .load(destFile)
                                    .placeholder(R.drawable.default_recipe)
                                    .error(R.drawable.default_recipe)
                                    .centerCrop()
                                    .into(recipeImageView)
                            }
                        } catch (e: Exception) {
                            Log.e("RecipeAdapter", "Failed to load image from content URI", e)
                            recipeImageView.setImageResource(R.drawable.default_recipe)
                        }
                    }
                } else {
                    // If already a file path, load directly from disk
                    Glide.with(context)
                        .load(File(imagePath))
                        .placeholder(R.drawable.default_recipe)
                        .error(R.drawable.default_recipe)
                        .centerCrop()
                        .into(recipeImageView)
                }
            } else {
                // No image, use default
                recipeImageView.setImageResource(R.drawable.default_recipe)
            }

            // Load user profile image from Room DB (if exists)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userImage = userImageDao.get(recipe.userId)
                    withContext(Dispatchers.Main) {
                        val profilePath = userImage?.imagePath
                        Glide.with(context)
                            .load(if (profilePath != null) File(profilePath) else R.drawable.default_profile_image)
                            .placeholder(R.drawable.default_profile_image)
                            .circleCrop()
                            .skipMemoryCache(true) // Ensures updated image is loaded
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable disk caching
                            .into(profileImageView)
                    }
                } catch (e: Exception) {
                    // Silently fail if image loading fails
                }
            }

            // Handle recipe item click
            itemView.setOnClickListener {
                onItemClick(recipe)
            }
        }
    }
}