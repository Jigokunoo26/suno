package com.deepak.suno.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores favorited song IDs locally using Room.
 * We store the MediaStore song ID rather than whole file paths so favorites
 * remain intact even if audio tag metadata is refreshed.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: Long,
    val addedAt: Long = System.currentTimeMillis()
)
