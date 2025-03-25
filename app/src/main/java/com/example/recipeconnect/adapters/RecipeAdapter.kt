package com.example.recipeconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.recipeconnect.R
import com.example.recipeconnect.models.Recipe

class RecipeAdapter(
    private var recipes: List<Recipe>, // Mutable list
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.bind(recipe)
    }

    override fun getItemCount() = recipes.size

    // 🔄 Function to update the list dynamically (used in LiveData observer)
    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    inner class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTextView: TextView = view.findViewById(R.id.recipeTitleTextView)
        private val descriptionTextView: TextView = view.findViewById(R.id.recipeDescriptionTextView)
        private val recipeImageView: ImageView = view.findViewById(R.id.recipeImageView)

        fun bind(recipe: Recipe) {
            titleTextView.text = recipe.title
            descriptionTextView.text = recipe.ingredients.joinToString(", ")
            Glide.with(itemView.context)
                .load(recipe.imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(recipeImageView)

            itemView.setOnClickListener { onItemClick(recipe) }
        }
    }
}