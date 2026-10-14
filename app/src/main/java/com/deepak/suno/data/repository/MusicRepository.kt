package com.deepak.suno.data.repository

import com.deepak.suno.data.local.entity.SongStatsEntity
import com.deepak.suno.domain.model.Album
import com.deepak.suno.domain.model.Artist
import com.deepak.suno.domain.model.Playlist
import com.deepak.suno.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

/**
 * Clean Architecture Repository interface for device music, favorites, playlists, and listening stats.
 */
interface MusicRepository {
    val songs: StateFlow<List<Song>>
    val albums: StateFlow<List<Album>>
    val artists: StateFlow<List<Artist>>
    val favorites: StateFlow<Set<Long>>
    val playlists: StateFlow<List<Playlist>>
    val songStats: StateFlow<Map<Long, SongStatsEntity>>
    val isLoading: StateFlow<Boolean>

    suspend fun refresh()
    fun search(query: String): List<Song>
    fun getSongsByAlbum(albumId: Long): List<Song>
    fun getSongsByArtist(artistName: String): List<Song>
    fun getSongById(id: Long): Song?

    // Favorites & Playlists (Room)
    suspend fun toggleFavorite(songId: Long)
    suspend fun isFavorite(songId: Long): Boolean
    suspend fun createPlaylist(name: String): Long
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    suspend fun deletePlaylist(playlistId: Long)

    // Listening Stats & Play History
    suspend fun recordSongPlay(songId: Long)
}

