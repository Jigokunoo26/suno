package com.deepak.suno.domain.model

import android.net.Uri

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val songCount: Int,
    val artworkUri: Uri?
)

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int
)
