package com.deepak.suno.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.deepak.suno.domain.model.PlayerState
import com.deepak.suno.domain.model.RepeatMode
import com.deepak.suno.domain.model.Song
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var currentPlaylist: List<Song> = emptyList()
    private var positionTickerJob: Job? = null
    private var sleepTimerJob: Job? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupPlayerListener()
                startPositionTicker()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun setupPlayerListener() {
        val controller = mediaController ?: return

        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startPositionTicker()
                } else {
                    stopPositionTicker()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val currentMediaId = mediaItem?.mediaId?.toLongOrNull()
                val currentSong = currentPlaylist.find { it.id == currentMediaId }

                _playerState.update {
                    it.copy(
                        currentSong = currentSong,
                        durationMs = controller.duration.coerceAtLeast(0L),
                        currentPositionMs = controller.currentPosition.coerceAtLeast(0L),
                        currentQueueIndex = controller.currentMediaItemIndex,
                        queueSize = controller.mediaItemCount
                    )
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _playerState.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        durationMs = controller.duration.coerceAtLeast(0L)
                    )
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playerState.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val mappedMode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
                _playerState.update { it.copy(repeatMode = mappedMode) }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("MusicController", "ExoPlayer playback error []: ", error)
                _playerState.update { it.copy(isPlaying = false, isBuffering = false) }
            }
        })
    }

    fun playSong(selectedSong: Song, songsQueue: List<Song>) {
        val controller = mediaController ?: return
        currentPlaylist = songsQueue

        val mediaItems = songsQueue.map { song ->
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setArtworkUri(song.artworkUri)
                .build()

            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.contentUri)
                .setMediaMetadata(metadata)
                .build()
        }

        val startIndex = songsQueue.indexOfFirst { it.id == selectedSong.id }.coerceAtLeast(0)

        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()

        _playerState.update {
            it.copy(
                currentSong = selectedSong,
                isPlaying = true,
                currentQueueIndex = startIndex,
                queueSize = songsQueue.size
            )
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun playPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _playerState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun skipToNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        }
    }

    fun skipToPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        }
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        val newShuffle = !controller.shuffleModeEnabled
        controller.shuffleModeEnabled = newShuffle
    }

    fun toggleRepeat() {
        val controller = mediaController ?: return
        val nextMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = nextMode
    }

    /**
     * Changes playback speed (e.g. 0.75x, 1.0x, 1.25x, 1.5x)
     */
    fun setPlaybackSpeed(speed: Float) {
        val controller = mediaController ?: return
        controller.setPlaybackSpeed(speed)
        _playerState.update { it.copy(playbackSpeed = speed) }
    }

    /**
     * Sets a sleep timer that automatically pauses playback after [minutes].
     */
    fun setSleepTimer(minutes: Int) {
        cancelSleepTimer()
        if (minutes <= 0) return

        _playerState.update { it.copy(sleepTimerMinutesLeft = minutes) }

        sleepTimerJob = scope.launch {
            var remaining = minutes
            while (remaining > 0 && isActive) {
                delay(60_000L) // 1 minute
                remaining--
                _playerState.update { it.copy(sleepTimerMinutesLeft = if (remaining > 0) remaining else null) }
            }
            // Timer expired: gently pause music
            pause()
            _playerState.update { it.copy(sleepTimerMinutesLeft = null) }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _playerState.update { it.copy(sleepTimerMinutesLeft = null) }
    }

    private fun startPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = scope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _playerState.update {
                            it.copy(
                                currentPositionMs = controller.currentPosition.coerceAtLeast(0L),
                                durationMs = controller.duration.coerceAtLeast(0L)
                            )
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
    }

    fun release() {
        cancelSleepTimer()
        stopPositionTicker()
        mediaController?.release()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
