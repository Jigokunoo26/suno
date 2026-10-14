package com.deepak.suno.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.deepak.suno.data.local.DownloadMetadataStore
import com.deepak.suno.domain.model.Album
import com.deepak.suno.domain.model.Artist
import com.deepak.suno.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MediaStoreScanner queries Android's MediaStore ContentProvider.
 *
 * Why MediaStore?
 * 1. Performance: Android's internal indexing daemon pre-scans file headers and tags.
 * 2. Scoped Storage: Direct file paths (/storage/emulated/0/...) are restricted in Android 10+.
 *    MediaStore gives content:// URIs which can be securely decoded without raw filesystem access.
 */
@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val metadataStore: DownloadMetadataStore
) {

    private val albumArtUriBase = Uri.parse("content://media/external/audio/albumart")

    /**
     * Scans all audio files flagged as music on the device.
     */
    suspend fun scanSongs(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        val contentResolver = context.contentResolver

        val collectionUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA
        )

        // Exclude system notification sounds, ringtones, and alarms while including
        // all music tracks (even freshly downloaded ones where duration calculation is in progress)
        val selection = "(${MediaStore.Audio.Media.IS_RINGTONE} == 0 AND " +
                "${MediaStore.Audio.Media.IS_NOTIFICATION} == 0 AND " +
                "${MediaStore.Audio.Media.IS_ALARM} == 0) AND " +
                "(${MediaStore.Audio.Media.DURATION} >= 3000 OR ${MediaStore.Audio.Media.DURATION} == 0 OR ${MediaStore.Audio.Media.DURATION} IS NULL)"

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(
            collectionUri,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val displayNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                var title = cursor.getString(titleColumn) ?: "Unknown Title"
                var artist = cursor.getString(artistColumn) ?: "<unknown>"
                var album = cursor.getString(albumColumn) ?: "<unknown>"
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val displayName = if (displayNameColumn != -1) cursor.getString(displayNameColumn) else null
                val filePath = if (dataColumn != -1) cursor.getString(dataColumn) else null

                // Build Scoped Storage Content URI
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Build Album Artwork URI
                var artworkUri: Uri? = ContentUris.withAppendedId(albumArtUriBase, albumId)

                // 1. Check if metadata was cached when downloaded via Suno
                val cachedMeta = metadataStore.getMetadata(title, displayName)
                if (cachedMeta != null) {
                    val cachedArtist = cachedMeta.optString("artist", "")
                    if (cachedArtist.isNotBlank() && !cachedArtist.equals("Unknown Artist", ignoreCase = true)) {
                        artist = cachedArtist
                    }
                    val cachedTitle = cachedMeta.optString("title", "")
                    if (cachedTitle.isNotBlank()) {
                        title = cachedTitle
                    }
                    val cachedArtworkUrl = cachedMeta.optString("artworkUrl", "")
                    if (cachedArtworkUrl.isNotBlank()) {
                        artworkUri = Uri.parse(cachedArtworkUrl)
                    }
                    if (album.contains("unknown", ignoreCase = true) || album.isBlank()) {
                        album = "Suno Music"
                    }
                }

                // 2. If artist is still unknown, try extracting from title or filename (e.g. "Artist - Title")
                if (artist.contains("unknown", ignoreCase = true) || artist.isBlank()) {
                    if (title.contains(" - ")) {
                        val parts = title.split(" - ")
                        if (parts.size >= 2) {
                            artist = parts[0].trim()
                            title = parts[1].trim()
                        }
                    } else if (displayName != null && displayName.contains(" - ")) {
                        val cleanName = displayName.replace(Regex("\\.(mp3|m4a|mp4|aac|flac|wav)$", RegexOption.IGNORE_CASE), "")
                        val parts = cleanName.split(" - ")
                        if (parts.size >= 2) {
                            artist = parts[0].trim()
                            title = parts[1].trim()
                        }
                    }
                }

                val finalArtist = if (artist.contains("unknown", ignoreCase = true) || artist.isBlank()) {
                    "Unknown Artist"
                } else {
                    artist
                }

                val finalAlbum = if (album.contains("unknown", ignoreCase = true) || album.isBlank()) {
                    "Unknown Album"
                } else {
                    album
                }

                val isDownloaded = (filePath != null && filePath.contains("Suno", ignoreCase = true)) ||
                        finalAlbum.equals("Suno Music", ignoreCase = true) ||
                        metadataStore.isDownloaded(title, displayName) ||
                        (cachedMeta != null)

                songs.add(
                    Song(
                        id = id,
                        title = title,
                        artist = finalArtist,
                        album = finalAlbum,
                        durationMs = duration,
                        contentUri = contentUri,
                        artworkUri = artworkUri,
                        albumId = albumId,
                        isDownloaded = isDownloaded
                    )
                )
            }
        }

        songs
    }

    /**
     * Aggregates scanned songs into albums.
     */
    fun extractAlbums(songs: List<Song>): List<Album> {
        return songs.groupBy { it.albumId }.map { (albumId, albumSongs) ->
            val first = albumSongs.first()
            Album(
                id = albumId,
                name = first.album,
                artist = first.artist,
                songCount = albumSongs.size,
                artworkUri = first.artworkUri
            )
        }.sortedBy { it.name }
    }

    /**
     * Aggregates scanned songs into artists.
     */
    fun extractArtists(songs: List<Song>): List<Artist> {
        return songs.groupBy { it.artist }.map { (artistName, artistSongs) ->
            Artist(
                name = artistName,
                songCount = artistSongs.size,
                albumCount = artistSongs.map { it.albumId }.distinct().size
            )
        }.sortedBy { it.name }
    }
}