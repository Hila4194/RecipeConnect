package com.recipeconnect.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.recipeconnect.databinding.ItemRecipeBinding
import com.recipeconnect.model.Recipe
import com.squareup.picasso.Picasso

class RecipeAdapter(private val recipeList: List<Recipe>, private val onClick: (Recipe) -> Unit) :
    RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.tvTitle.text = recipe.title
            binding.tvCuisine.text = "Cuisine: ${recipe.cuisine}"
            binding.tvTime.text = "Time: ${recipe.time}"
            binding.tvDifficulty.text = "Difficulty: ${recipe.difficulty}"

            // Load image with Picasso
            Picasso.get().load(recipe.imageUrl).into(binding.ivRecipe)

            binding.root.setOnClickListener { onClick(recipe) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipeList[position])
    }

    override fun getItemCount(): Int = recipeList.size
}
