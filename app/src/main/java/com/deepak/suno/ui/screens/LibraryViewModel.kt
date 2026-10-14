package com.deepak.suno.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepak.suno.data.local.entity.SongStatsEntity
import com.deepak.suno.data.repository.MusicRepository
import com.deepak.suno.data.youtube.YouTubeService
import com.deepak.suno.domain.model.Album
import com.deepak.suno.domain.model.Artist
import com.deepak.suno.domain.model.DownloadStatus
import com.deepak.suno.domain.model.PlayerState
import com.deepak.suno.domain.model.Playlist
import com.deepak.suno.domain.model.Song
import com.deepak.suno.domain.model.YouTubeTrack
import com.deepak.suno.player.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab {
    HOME,
    SONGS,
    DOWNLOADS,
    FAVORITES,
    ALBUMS,
    ARTISTS,
    PLAYLISTS,
    YOUTUBE
}

data class LibraryUiState(
    val selectedTab: LibraryTab = LibraryTab.HOME,
    val searchQuery: String = "",
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val favorites: Set<Long> = emptySet(),
    val playlists: List<Playlist> = emptyList(),
    val songStats: Map<Long, SongStatsEntity> = emptyMap(),
    val youtubeTracks: List<YouTubeTrack> = emptyList(),
    val isSearchingYouTube: Boolean = false,
    val youtubeUrlInput: String = "",
    val isLoading: Boolean = false,
    val playerState: PlayerState = PlayerState(),
    val isNowPlayingSheetVisible: Boolean = false,
    val activePlaylistId: Long? = null,
    val activeAlbumId: Long? = null,
    val songForAddToPlaylist: Song? = null
) {
    val activePlaylist: Playlist?
        get() = playlists.firstOrNull { it.id == activePlaylistId }

    val activeAlbum: Album?
        get() = albums.firstOrNull { it.id == activeAlbumId }

    val favoriteSongs: List<Song> by lazy { songs.filter { favorites.contains(it.id) } }

    val downloadedSongs: List<Song> by lazy { songs.filter { it.isDownloaded } }

    val mostPlayedSongs: List<Song> by lazy {
        if (songStats.isNotEmpty()) {
            val played = songs.filter { songStats.containsKey(it.id) }
                .sortedByDescending { songStats[it.id]?.playCount ?: 0 }
            if (played.isNotEmpty()) {
                val others = songs.filterNot { songStats.containsKey(it.id) }
                (played + others).take(20)
            } else {
                songs.take(20)
            }
        } else {
            songs.take(20)
        }
    }

    val recentlyPlayedSongs: List<Song> by lazy {
        if (songStats.isNotEmpty()) {
            val played = songs.filter { songStats.containsKey(it.id) }
                .sortedByDescending { songStats[it.id]?.lastPlayedTimestamp ?: 0 }
            if (played.isNotEmpty()) {
                played.take(15)
            } else {
                favoriteSongs.ifEmpty { songs.take(15) }
            }
        } else {
            favoriteSongs.ifEmpty { songs.take(15) }
        }
    }
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val musicController: MusicController,
    private val youTubeService: YouTubeService
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LibraryTab.HOME)
    private val _searchQuery = MutableStateFlow("")
    private val _isNowPlayingVisible = MutableStateFlow(false)

    // Active detail views and playlist management
    private val _activePlaylistId = MutableStateFlow<Long?>(null)
    private val _activeAlbumId = MutableStateFlow<Long?>(null)
    private val _songForAddToPlaylist = MutableStateFlow<Song?>(null)

    // YouTube state
    private val _youtubeTracks = MutableStateFlow<List<YouTubeTrack>>(emptyList())
    private val _isSearchingYouTube = MutableStateFlow(false)
    private val _youtubeUrlInput = MutableStateFlow("")

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        _selectedTab,
        _searchQuery,
        repository.songs,
        repository.albums,
        repository.artists,
        repository.favorites,
        repository.playlists,
        repository.songStats,
        _youtubeTracks,
        _isSearchingYouTube,
        _youtubeUrlInput,
        repository.isLoading,
        musicController.playerState,
        _isNowPlayingVisible,
        _activePlaylistId,
        _activeAlbumId,
        _songForAddToPlaylist
    ) { params: Array<Any?> ->
        val selectedTab = params[0] as LibraryTab
        val query = params[1] as String
        @Suppress("UNCHECKED_CAST")
        val songs = params[2] as List<Song>
        @Suppress("UNCHECKED_CAST")
        val albums = params[3] as List<Album>
        @Suppress("UNCHECKED_CAST")
        val artists = params[4] as List<Artist>
        @Suppress("UNCHECKED_CAST")
        val favorites = params[5] as Set<Long>
        @Suppress("UNCHECKED_CAST")
        val playlists = params[6] as List<Playlist>
        @Suppress("UNCHECKED_CAST")
        val songStats = params[7] as Map<Long, SongStatsEntity>
        @Suppress("UNCHECKED_CAST")
        val youtubeTracks = params[8] as List<YouTubeTrack>
        val isSearchingYouTube = params[9] as Boolean
        val youtubeUrlInput = params[10] as String
        val isLoading = params[11] as Boolean
        val playerState = params[12] as PlayerState
        val isNowPlayingVisible = params[13] as Boolean
        val activePlaylistId = params[14] as? Long
        val activeAlbumId = params[15] as? Long
        val songForAddToPlaylist = params[16] as? Song

        val filteredSongs = if (query.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }
        }

        LibraryUiState(
            selectedTab = selectedTab,
            searchQuery = query,
            songs = filteredSongs,
            albums = albums,
            artists = artists,
            favorites = favorites,
            playlists = playlists,
            songStats = songStats,
            youtubeTracks = youtubeTracks,
            isSearchingYouTube = isSearchingYouTube,
            youtubeUrlInput = youtubeUrlInput,
            isLoading = isLoading,
            playerState = playerState,
            isNowPlayingSheetVisible = isNowPlayingVisible,
            activePlaylistId = activePlaylistId,
            activeAlbumId = activeAlbumId,
            songForAddToPlaylist = songForAddToPlaylist
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun onTabSelected(tab: LibraryTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun playSong(song: Song) {
        val currentQueue = when (uiState.value.selectedTab) {
            LibraryTab.FAVORITES -> uiState.value.favoriteSongs
            LibraryTab.DOWNLOADS -> uiState.value.downloadedSongs
            LibraryTab.HOME -> uiState.value.mostPlayedSongs.ifEmpty { uiState.value.songs }
            else -> uiState.value.songs
        }
        musicController.playSong(song, currentQueue)
        viewModelScope.launch {
            repository.recordSongPlay(song.id)
        }
    }

    fun playQueue(queue: List<Song>, startIndex: Int = 0) {
        if (queue.isNotEmpty()) {
            val safeIndex = startIndex.coerceIn(0, queue.size - 1)
            musicController.playSong(queue[safeIndex], queue)
            viewModelScope.launch {
                repository.recordSongPlay(queue[safeIndex].id)
            }
        }
    }

    fun playAllDownloads(shuffle: Boolean = false) {
        val downloads = uiState.value.downloadedSongs
        if (downloads.isNotEmpty()) {
            val queue = if (shuffle) downloads.shuffled() else downloads
            musicController.playSong(queue.first(), queue)
            viewModelScope.launch {
                repository.recordSongPlay(queue.first().id)
            }
        }
    }

    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(songId)
        }
    }

    fun createPlaylist(name: String, initialSongIds: List<Long> = emptyList()) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return
        viewModelScope.launch {
            val id = repository.createPlaylist(cleanName)
            if (initialSongIds.isNotEmpty()) {
                repository.addSongsToPlaylist(id, initialSongIds)
            }
            _userMessage.emit("Playlist '$cleanName' created")
            _activePlaylistId.value = id
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
            _userMessage.emit("Added to playlist")
            _songForAddToPlaylist.value = null
        }
    }

    fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        if (songIds.isEmpty()) return
        viewModelScope.launch {
            repository.addSongsToPlaylist(playlistId, songIds)
            _userMessage.emit("Added ${songIds.size} songs to playlist")
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
            _userMessage.emit("Removed from playlist")
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_activePlaylistId.value == playlistId) {
                _activePlaylistId.value = null
            }
            _userMessage.emit("Playlist deleted")
        }
    }

    fun openPlaylist(playlistId: Long?) {
        _activePlaylistId.value = playlistId
    }

    fun openAlbum(albumId: Long?) {
        _activeAlbumId.value = albumId
    }

    fun setSongForAddToPlaylist(song: Song?) {
        _songForAddToPlaylist.value = song
    }

    // --- YouTube Operations ---

    fun onYouTubeUrlInputChanged(url: String) {
        _youtubeUrlInput.value = url
    }

    fun searchYouTube(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isSearchingYouTube.value = true
            try {
                val results = youTubeService.search(query)
                _youtubeTracks.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSearchingYouTube.value = false
            }
        }
    }

    fun streamYouTubeTrack(track: YouTubeTrack) {
        viewModelScope.launch {
            updateTrackStreamingStatus(track.videoId, isLoading = true)
            _userMessage.emit("Connecting stream for: ${track.title}")
            try {
                val streamUrl = track.streamUrl ?: youTubeService.resolveStreamUrl(track.videoId, track.title)
                val song = track.toSong(Uri.parse(streamUrl))
                musicController.playSong(song, listOf(song))
                _userMessage.emit("Playing: ${track.title}")
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = e.message ?: "Unable to stream track"
                _userMessage.emit("Stream failed: $errorMsg")
                updateTrackError(track.videoId, errorMsg)
            } finally {
                updateTrackStreamingStatus(track.videoId, isLoading = false)
            }
        }
    }

    fun downloadYouTubeTrack(track: YouTubeTrack) {
        viewModelScope.launch {
            updateTrackDownloadStatus(track.videoId, DownloadStatus.DOWNLOADING, 0f)
            _userMessage.emit("Downloading: ${track.title}...")
            val result = youTubeService.downloadAudio(track) { progress ->
                updateTrackDownloadStatus(track.videoId, DownloadStatus.DOWNLOADING, progress)
            }
            if (result.isSuccess) {
                val savedFile = result.getOrNull()
                updateTrackDownloadStatus(track.videoId, DownloadStatus.COMPLETED, 1f)
                _userMessage.emit("Downloaded: ${savedFile?.name ?: track.title} saved to Music/Suno!")
                kotlinx.coroutines.delay(300)
                // Refresh local device songs so the new downloaded file immediately appears!
                repository.refresh()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Download failed"
                updateTrackDownloadStatus(track.videoId, DownloadStatus.ERROR, 0f, errorMsg)
                _userMessage.emit("Download failed: $errorMsg")
            }
        }
    }

    private fun updateTrackStreamingStatus(videoId: String, isLoading: Boolean) {
        val updated = _youtubeTracks.value.map {
            if (it.videoId == videoId) {
                it.copy(isStreamingLoading = isLoading)
            } else {
                it
            }
        }
        _youtubeTracks.value = updated
    }

    private fun updateTrackDownloadStatus(
        videoId: String,
        status: DownloadStatus,
        progress: Float,
        errorMessage: String? = null
    ) {
        val updated = _youtubeTracks.value.map {
            if (it.videoId == videoId) {
                it.copy(
                    downloadStatus = status,
                    downloadProgress = progress,
                    errorMessage = errorMessage
                )
            } else {
                it
            }
        }
        _youtubeTracks.value = updated
    }

    private fun updateTrackError(videoId: String, errorMsg: String) {
        val updated = _youtubeTracks.value.map {
            if (it.videoId == videoId) {
                it.copy(errorMessage = errorMsg)
            } else {
                it
            }
        }
        _youtubeTracks.value = updated
    }

    // --- Player Controls ---

    fun playPause() {
        musicController.playPause()
    }

    fun seekTo(positionMs: Long) {
        musicController.seekTo(positionMs)
    }

    fun skipNext() {
        musicController.skipToNext()
    }

    fun skipPrevious() {
        musicController.skipToPrevious()
    }

    fun toggleShuffle() {
        musicController.toggleShuffle()
    }

    fun toggleRepeat() {
        musicController.toggleRepeat()
    }

    fun setPlaybackSpeed(speed: Float) {
        musicController.setPlaybackSpeed(speed)
    }

    fun setSleepTimer(minutes: Int) {
        musicController.setSleepTimer(minutes)
    }

    fun setNowPlayingSheetVisible(visible: Boolean) {
        _isNowPlayingVisible.value = visible
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refresh()
        }
    }
}
