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
    private var recipes: List<Recipe>,
    private val emailMap: Map<String, String>,
    private val context: Context,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    private val userImageDao = RecipeDatabase.getDatabase(context).userImageDao()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount() = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
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

            if (!imagePath.isNullOrEmpty()) {
                if (imagePath.startsWith("content://")) {
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
                    // Already local file path
                    Glide.with(context)
                        .load(File(imagePath))
                        .placeholder(R.drawable.default_recipe)
                        .error(R.drawable.default_recipe)
                        .centerCrop()
                        .into(recipeImageView)
                }
            } else {
                recipeImageView.setImageResource(R.drawable.default_recipe)
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userImage = userImageDao.get(recipe.userId)
                    withContext(Dispatchers.Main) {
                        val profilePath = userImage?.imagePath
                        Glide.with(context)
                            .load(if (profilePath != null) File(profilePath) else R.drawable.default_profile_image)
                            .placeholder(R.drawable.default_profile_image)
                            .circleCrop()
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(profileImageView)
                    }
                } catch (e: Exception) {
                    // Silent fail
                }
            }

            itemView.setOnClickListener { onItemClick(recipe) }
        }
    }
}