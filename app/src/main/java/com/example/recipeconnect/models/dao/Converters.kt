package com.example.recipeconnect.models.dao

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromListToString(list: List<String>): String {
        return list.joinToString("|||") // Use uncommon delimiter to avoid conflicts
    }

    @TypeConverter
    fun fromStringToList(data: String): List<String> {
        return if (data.isEmpty()) emptyList() else data.split("|||")
    }
}