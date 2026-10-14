package com.deepak.suno.domain.model

import android.net.Uri
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Domain representation of an audio track on the device.
 * Using Content URIs adheres to Android's Scoped Storage guidelines.
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: Uri,
    val artworkUri: Uri?,
    val albumId: Long
) {
    /**
     * Formats duration into a human-readable mm:ss string.
     */
    fun formattedDuration(): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}
