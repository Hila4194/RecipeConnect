package com.example.recipeconnect.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.recipeconnect.models.dao.UserImage

@Dao
interface UserImageDao {
    @Insert
    suspend fun insert(userImage: UserImage)

    @Query("SELECT * FROM user_images WHERE uid = :uid")
    suspend fun get(uid: String): UserImage?
}
