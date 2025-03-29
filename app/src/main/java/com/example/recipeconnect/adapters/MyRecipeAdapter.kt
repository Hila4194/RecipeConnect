package com.example.recipeconnect.adapters

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.*
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
    private var recipes: List<Recipe>,                    // List of recipes to display
    private val context: Context,                         // Context needed for Glide and contentResolver
    private val onDeleteClick: (Recipe) -> Unit,          // Callback for delete action
    private val onEditClick: (Recipe) -> Unit,            // Callback for edit action
    private val onItemClick: (Recipe) -> Unit,            // Callback for clicking an item (used in favorites)
    private val isEditable: Boolean = true,               // Determines if edit/delete options are shown
    private val isFavorite: Boolean = false               // Indicates if this is the favorites list
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

            val imagePath = recipe.imageUrl
            Log.d("MyRecipeAdapter", "Image path: $imagePath")

            // Load image from content URI or file path
            if (!imagePath.isNullOrEmpty()) {
                if (imagePath.startsWith("content://")) {
                    // Image selected from gallery (content URI)
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
                    // Image saved locally with file path
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

            // Favorite mode: allow item click (open details)
            if (isFavorite) {
                itemView.setOnClickListener { onItemClick(recipe) }
            } else {
                // In "My Recipes" mode, item itself is not clickable
                recipeImageView.isClickable = false
                recipeImageView.isFocusable = false
                titleTextView.isClickable = false
                titleTextView.isFocusable = false
            }

            // Handle edit/delete icons
            if (isEditable) {
                editIcon.visibility = View.VISIBLE
                deleteIcon.visibility = View.VISIBLE

                editIcon.setOnClickListener {
                    onEditClick(recipe)
                }

                deleteIcon.setOnClickListener {
                    AlertDialog.Builder(context)
                        .setTitle("Delete Recipe")
                        .setMessage("Are you sure you want to delete this recipe?")
                        .setPositiveButton("Yes") { _, _ ->
                            onDeleteClick(recipe)
                        }
                        .setNegativeButton("No", null)
                        .show()
                }
            } else {
                // In favorites mode, hide icons
                editIcon.visibility = View.GONE
                deleteIcon.visibility = View.GONE
            }
        }
    }
}