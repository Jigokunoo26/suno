package com.deepak.suno.domain.model

import android.net.Uri
import java.util.Locale

enum class DownloadStatus {
    IDLE,
    DOWNLOADING,
    COMPLETED,
    ERROR
}

data class YouTubeTrack(
    val videoId: String,
    val title: String,
    val uploader: String,
    val durationSeconds: Long,
    val thumbnailUrl: String,
    val streamUrl: String? = null,
    val downloadStatus: DownloadStatus = DownloadStatus.IDLE,
    val downloadProgress: Float = 0f,
    val isStreamingLoading: Boolean = false,
    val errorMessage: String? = null
) {
    fun formattedDuration(): String {
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", mins, secs)
    }

    /**
     * Converts an online YouTube track to a Song domain model for playback in ExoPlayer.
     */
    fun toSong(resolvedStreamUri: Uri): Song {
        return Song(
            id = videoId.hashCode().toLong(),
            title = title,
            artist = uploader,
            album = "YouTube Music",
            durationMs = durationSeconds * 1000L,
            contentUri = resolvedStreamUri,
            artworkUri = Uri.parse(thumbnailUrl),
            albumId = -1L
        )
    }
}
