package com.deepak.suno.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
    @ApplicationContext private val context: Context
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
            MediaStore.Audio.Media.ALBUM_ID
        )

        // Filter out ringtones, notification chimes, and zero-duration corrupted files
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
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

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown Title"
                val artist = cursor.getString(artistColumn) ?: "<unknown>"
                val album = cursor.getString(albumColumn) ?: "<unknown>"
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)

                // Build Scoped Storage Content URI
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // Build Album Artwork URI
                val artworkUri = ContentUris.withAppendedId(albumArtUriBase, albumId)

                songs.add(
                    Song(
                        id = id,
                        title = title,
                        artist = if (artist.contains("unknown", ignoreCase = true)) "Unknown Artist" else artist,
                        album = if (album.contains("unknown", ignoreCase = true)) "Unknown Album" else album,
                        durationMs = duration,
                        contentUri = contentUri,
                        artworkUri = artworkUri,
                        albumId = albumId
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
