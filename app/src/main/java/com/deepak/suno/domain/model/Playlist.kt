package com.deepak.suno.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val songs: List<Song> = emptyList()
)
