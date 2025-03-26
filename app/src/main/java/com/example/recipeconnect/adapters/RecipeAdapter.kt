package com.example.recipeconnect.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.models.Recipe
import com.example.recipeconnect.models.dao.RecipeDatabase
import kotlinx.coroutines.*
import java.io.File

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

            Glide.with(itemView.context)
                .load(recipe.imageUrl)
                .placeholder(R.drawable.default_recipe)
                .into(recipeImageView)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userImage = userImageDao.get(recipe.userId)
                    withContext(Dispatchers.Main) {
                        val imagePath = userImage?.imagePath
                        Glide.with(itemView.context)
                            .load(if (imagePath != null) File(imagePath) else R.drawable.default_profile_image)
                            .placeholder(R.drawable.default_profile_image)
                            .circleCrop()
                            .skipMemoryCache(true) // ✅ Force reload
                            .into(profileImageView)
                    }
                } catch (e: Exception) {
                    // Fail silently
                }
            }

            itemView.setOnClickListener { onItemClick(recipe) }
        }
    }
}