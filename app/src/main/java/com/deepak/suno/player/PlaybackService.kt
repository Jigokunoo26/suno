package com.deepak.suno.player

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.deepak.suno.MainActivity

/**
 * PlaybackService extends MediaSessionService from AndroidX Media3.
 *
 * Why a Service instead of an Activity?
 * 1. Background Playback: Survives when the user leaves the app, locks the screen, or rotates device.
 * 2. System Notification: Media3 automatically builds and manages lock-screen media controls
 *    and system notifications with play/pause/prev/next/seek.
 * 3. Audio Focus: Automatic pausing when an incoming phone call occurs or GPS voice speaks.
 * 4. External Peripherals: Responds to Bluetooth headphone buttons, Android Auto, and smartwatches.
 */
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // Configure audio attributes for music playback with automatic audio focus handling
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val httpDataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            .setUserAgent("com.google.android.youtube/20.10.38 (Linux; U; Android 14; en_US) gzip")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)

        // DefaultDataSource routes content:// to ContentDataSource, file:// to FileDataSource,
        // and http(s):// to DefaultHttpDataSource. This allows playing local downloaded tracks and streams seamlessly.
        val dataSourceFactory = androidx.media3.datasource.DefaultDataSource.Factory(this, httpDataSourceFactory)

        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(this)
            .setDataSourceFactory(dataSourceFactory)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true) // Pauses music when wired headphones are unplugged
            .setWakeMode(C.WAKE_MODE_LOCAL)    // Prevents CPU from sleeping during playback
            .build()

        // PendingIntent to launch MainActivity when clicking the media notification
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}