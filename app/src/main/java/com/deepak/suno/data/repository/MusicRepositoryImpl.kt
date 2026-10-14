package com.deepak.suno.data.repository

import com.deepak.suno.data.local.dao.FavoriteDao
import com.deepak.suno.data.local.dao.PlaylistDao
import com.deepak.suno.data.local.entity.FavoriteEntity
import com.deepak.suno.data.local.entity.PlaylistEntity
import com.deepak.suno.data.local.entity.PlaylistSongCrossRef
import com.deepak.suno.data.mediastore.MediaStoreScanner
import com.deepak.suno.domain.model.Album
import com.deepak.suno.domain.model.Artist
import com.deepak.suno.domain.model.Playlist
import com.deepak.suno.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val scanner: MediaStoreScanner,
    private val favoriteDao: FavoriteDao,
    private val playlistDao: PlaylistDao
) : MusicRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    override val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    override val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    override val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Observe Room Favorites table as a reactive StateFlow of song IDs
    override val favorites: StateFlow<Set<Long>> = favoriteDao.getAllFavoriteIds()
        .map { it.toSet() }
        .stateIn(scope, SharingStarted.Eagerly, emptySet())

    // Observe Room Playlists table as a reactive StateFlow
    override val playlists: StateFlow<List<Playlist>> = playlistDao.getAllPlaylists()
        .map { entities ->
            entities.map { entity ->
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    songCount = 0 // updated dynamically when viewed
                )
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        scope.launch {
            refresh()
        }
    }

    override suspend fun refresh() {
        _isLoading.value = true
        try {
            val scannedSongs = scanner.scanSongs()
            _songs.value = scannedSongs
            _albums.value = scanner.extractAlbums(scannedSongs)
            _artists.value = scanner.extractArtists(scannedSongs)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    override fun search(query: String): List<Song> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return _songs.value
        return _songs.value.filter { song ->
            song.title.contains(cleanQuery, ignoreCase = true) ||
            song.artist.contains(cleanQuery, ignoreCase = true) ||
            song.album.contains(cleanQuery, ignoreCase = true)
        }
    }

    override fun getSongsByAlbum(albumId: Long): List<Song> {
        return _songs.value.filter { it.albumId == albumId }
    }

    override fun getSongsByArtist(artistName: String): List<Song> {
        return _songs.value.filter { it.artist.equals(artistName, ignoreCase = true) }
    }

    override fun getSongById(id: Long): Song? {
        return _songs.value.find { it.id == id }
    }

    // --- Room Database Operations ---

    override suspend fun toggleFavorite(songId: Long) {
        if (favoriteDao.isFavorite(songId)) {
            favoriteDao.removeFavorite(songId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(songId = songId))
        }
    }

    override suspend fun isFavorite(songId: Long): Boolean {
        return favoriteDao.isFavorite(songId)
    }

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name.trim()))
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(
            PlaylistSongCrossRef(playlistId = playlistId, songId = songId)
        )
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId = playlistId, songId = songId)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }
}
