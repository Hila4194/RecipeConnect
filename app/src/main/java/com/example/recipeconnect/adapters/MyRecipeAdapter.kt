package com.example.recipeconnect.adapters

import android.app.AlertDialog
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
import java.io.File

class MyRecipeAdapter(
    private var recipes: List<Recipe>,
    private val context: Context,
    private val onDeleteClick: (Recipe) -> Unit,
    private val onEditClick: (Recipe) -> Unit,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<MyRecipeAdapter.MyRecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_recipe, parent, false)
        return MyRecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyRecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount() = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    inner class MyRecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val recipeImageView: ImageView = view.findViewById(R.id.recipeImageView)
        private val titleTextView: TextView = view.findViewById(R.id.recipeTitleTextView)
        private val editIcon: ImageView = view.findViewById(R.id.editIcon)
        private val deleteIcon: ImageView = view.findViewById(R.id.deleteIcon)

        fun bind(recipe: Recipe) {
            titleTextView.text = recipe.title

            // 🔥 FIX: Load local image path from File if it exists
            val imageFile = File(recipe.imageUrl ?: "")
            Glide.with(context)
                .load(recipe.imageUrl ?: "")
                .placeholder(R.drawable.default_recipe)
                .error(R.drawable.default_recipe)
                .centerCrop()
                .into(recipeImageView)

            itemView.setOnClickListener {
                onItemClick(recipe)
            }

            editIcon.setOnClickListener {
                onEditClick(recipe)
            }

            deleteIcon.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete this recipe?")
                    .setPositiveButton("Yes") { _, _ -> onDeleteClick(recipe) }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }
}