package com.deepak.suno.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to track song listening frequency and playback statistics.
 * Powers the "Most Listened" ("Heavy Rotation / On Repeat") and "Jump Back In" sections.
 */
@Entity(tableName = "song_stats")
data class SongStatsEntity(
    @PrimaryKey
    val songId: Long,
    val playCount: Int = 1,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)
