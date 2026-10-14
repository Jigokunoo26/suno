package com.deepak.suno.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.deepak.suno.data.local.dao.FavoriteDao
import com.deepak.suno.data.local.dao.PlaylistDao
import com.deepak.suno.data.local.entity.FavoriteEntity
import com.deepak.suno.data.local.entity.PlaylistEntity
import com.deepak.suno.data.local.entity.PlaylistSongCrossRef

@Database(
    entities = [
        FavoriteEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SunoDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
}
