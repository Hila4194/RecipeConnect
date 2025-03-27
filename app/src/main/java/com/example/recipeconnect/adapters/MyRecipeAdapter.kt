package com.example.recipeconnect.adapters

import android.app.AlertDialog
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
import com.example.recipeconnect.R
import com.example.recipeconnect.models.Recipe
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlinx.coroutines.*

class MyRecipeAdapter(
    private var recipes: List<Recipe>,
    private val context: Context,
    private val onDeleteClick: (Recipe) -> Unit,
    private val onEditClick: (Recipe) -> Unit,
    private val onItemClick: (Recipe) -> Unit,
    private val isEditable: Boolean = true, // Default is editable
    private val isFavorite: Boolean = false // Flag to determine if the item is in FavoriteRecipes
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

            // Handle image loading
            val imagePath = recipe.imageUrl
            Log.d("MyRecipeAdapter", "Image path: $imagePath")

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
                            Log.e("MyRecipeAdapter", "Error loading content URI image", e)
                            recipeImageView.setImageResource(R.drawable.default_recipe)
                        }
                    }
                } else {
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

            // Handle item click for Favorite Recipes
            if (isFavorite) {
                itemView.setOnClickListener { onItemClick(recipe) } // Recipe item is clickable in Favorite Recipes
            } else {
                // In My Recipes, disable click on the item
                recipeImageView.isClickable = false
                recipeImageView.isFocusable = false
                titleTextView.isClickable = false
                titleTextView.isFocusable = false
            }

            // Edit and delete icons are only shown in editable mode (My Recipes)
            if (isEditable) {
                editIcon.visibility = View.VISIBLE
                deleteIcon.visibility = View.VISIBLE

                editIcon.setOnClickListener { onEditClick(recipe) }

                deleteIcon.setOnClickListener {
                    AlertDialog.Builder(context)
                        .setTitle("Delete Recipe")
                        .setMessage("Are you sure you want to delete this recipe?")
                        .setPositiveButton("Yes") { _, _ -> onDeleteClick(recipe) }
                        .setNegativeButton("No", null)
                        .show()
                }
            } else {
                // Hide edit/delete icons for Favorite Recipes
                editIcon.visibility = View.GONE
                deleteIcon.visibility = View.GONE
            }
        }
    }
}