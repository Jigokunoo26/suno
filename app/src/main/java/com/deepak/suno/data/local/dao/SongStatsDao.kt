package com.deepak.suno.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.deepak.suno.data.local.entity.SongStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongStatsDao {
    @Query("SELECT * FROM song_stats ORDER BY playCount DESC, lastPlayedTimestamp DESC")
    fun getAllStats(): Flow<List<SongStatsEntity>>

    @Query("SELECT * FROM song_stats WHERE songId = :songId")
    suspend fun getStatsForSong(songId: Long): SongStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: SongStatsEntity)

    @Query("UPDATE song_stats SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE songId = :songId")
    suspend fun incrementPlayCount(songId: Long, timestamp: Long = System.currentTimeMillis()): Int
}
