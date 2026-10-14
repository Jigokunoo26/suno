package com.deepak.suno.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deepak.suno.R
import com.deepak.suno.domain.model.Album
import com.deepak.suno.domain.model.Artist
import com.deepak.suno.domain.model.Playlist
import com.deepak.suno.domain.model.Song
import com.deepak.suno.domain.model.YouTubeTrack
import com.deepak.suno.ui.components.SongListItem
import com.deepak.suno.ui.components.YouTubeTrackItem
import com.deepak.suno.ui.theme.DarkSlateSurface
import com.deepak.suno.ui.theme.DarkSurfaceVariant
import com.deepak.suno.ui.theme.ElectricViolet
import com.deepak.suno.ui.theme.HotRose
import com.deepak.suno.ui.theme.MidnightObsidian
import com.deepak.suno.ui.theme.NeonCyan
import com.deepak.suno.ui.theme.TextPrimary
import com.deepak.suno.ui.theme.TextSecondary

@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    onTabSelected: (LibraryTab) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSongClicked: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onSearchYouTube: (String) -> Unit,
    onStreamYouTube: (YouTubeTrack) -> Unit,
    onDownloadYouTube: (YouTubeTrack) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var ytSearchInput by remember { mutableStateOf("") }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("New Playlist", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    placeholder = { Text("Playlist name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ElectricViolet
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSlateSurface
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightObsidian)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ElectricViolet),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LibraryMusic,
                        contentDescription = "Logo",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Suno",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (uiState.selectedTab == LibraryTab.YOUTUBE) "YouTube Stream & MP3" else "${uiState.songs.size} tracks available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.selectedTab == LibraryTab.YOUTUBE) NeonCyan else TextSecondary
                    )
                }
            }

            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh library",
                    tint = TextSecondary
                )
            }
        }

        // Local Search Bar (Only shown for local tabs)
        if (uiState.selectedTab != LibraryTab.YOUTUBE) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = {
                    Text(text = "Search songs, albums, artists...", color = TextSecondary)
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear search", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSlateSurface,
                    unfocusedContainerColor = DarkSlateSurface,
                    focusedBorderColor = ElectricViolet,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Navigation Tabs: Songs, Favorites, Albums, Artists, Playlists, YouTube
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            containerColor = MidnightObsidian,
            contentColor = ElectricViolet,
            edgePadding = 20.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal]),
                    color = if (uiState.selectedTab == LibraryTab.YOUTUBE) HotRose else ElectricViolet
                )
            },
            divider = {}
        ) {
            LibraryTab.values().forEach { tab ->
                val selected = uiState.selectedTab == tab
                val tabColor = if (tab == LibraryTab.YOUTUBE && selected) HotRose
                               else if (selected) ElectricViolet
                               else TextSecondary

                Tab(
                    selected = selected,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (tab == LibraryTab.YOUTUBE) {
                                Icon(
                                    imageVector = Icons.Rounded.SmartDisplay,
                                    contentDescription = null,
                                    tint = tabColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = if (tab == LibraryTab.YOUTUBE) "YouTube" else tab.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp,
                                color = tabColor
                            )
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Body Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricViolet)
            }
        } else {
            when (uiState.selectedTab) {
                LibraryTab.SONGS -> {
                    if (uiState.songs.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.LibraryMusic,
                            title = "No songs found",
                            subtitle = "Make sure your music files are placed in device storage"
                        )
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                            items(items = uiState.songs, key = { it.id }) { song ->
                                val isCurrent = uiState.playerState.currentSong?.id == song.id
                                val isFav = uiState.favorites.contains(song.id)
                                SongListItem(
                                    song = song,
                                    isCurrentSong = isCurrent,
                                    isPlaying = uiState.playerState.isPlaying,
                                    isFavorite = isFav,
                                    onToggleFavorite = { onToggleFavorite(song.id) },
                                    onClick = { onSongClicked(song) }
                                )
                            }
                        }
                    }
                }

                LibraryTab.FAVORITES -> {
                    val favSongs = uiState.favoriteSongs
                    if (favSongs.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.Favorite,
                            title = "No favorites yet",
                            subtitle = "Tap the heart icon on any song to add it here"
                        )
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                            items(items = favSongs, key = { it.id }) { song ->
                                val isCurrent = uiState.playerState.currentSong?.id == song.id
                                SongListItem(
                                    song = song,
                                    isCurrentSong = isCurrent,
                                    isPlaying = uiState.playerState.isPlaying,
                                    isFavorite = true,
                                    onToggleFavorite = { onToggleFavorite(song.id) },
                                    onClick = { onSongClicked(song) }
                                )
                            }
                        }
                    }
                }

                LibraryTab.ALBUMS -> {
                    if (uiState.albums.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.Album,
                            title = "No albums found",
                            subtitle = "Your audio files do not contain indexed album tags"
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(items = uiState.albums, key = { it.id }) { album ->
                                AlbumGridCard(album = album)
                            }
                        }
                    }
                }

                LibraryTab.ARTISTS -> {
                    if (uiState.artists.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.Person,
                            title = "No artists found",
                            subtitle = "Your audio files do not contain indexed artist tags"
                        )
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                            items(items = uiState.artists, key = { it.name }) { artist ->
                                ArtistListItem(artist = artist)
                            }
                        }
                    }
                }

                LibraryTab.PLAYLISTS -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Playlists",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Button(
                                onClick = { showCreatePlaylistDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New")
                            }
                        }

                        if (uiState.playlists.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                                title = "No playlists created",
                                subtitle = "Click 'New' above to create your first custom playlist"
                            )
                        } else {
                            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                                items(items = uiState.playlists, key = { it.id }) { playlist ->
                                    PlaylistListItem(playlist = playlist)
                                }
                            }
                        }
                    }
                }

                LibraryTab.YOUTUBE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // YouTube Search & URL Input Box
                        OutlinedTextField(
                            value = ytSearchInput,
                            onValueChange = { ytSearchInput = it },
                            placeholder = {
                                Text("Search song or paste YouTube URL...", color = TextSecondary)
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Rounded.SmartDisplay, contentDescription = null, tint = HotRose)
                            },
                            trailingIcon = {
                                Button(
                                    onClick = { onSearchYouTube(ytSearchInput) },
                                    colors = ButtonDefaults.buttonColors(containerColor = HotRose),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(end = 4.dp).height(36.dp)
                                ) {
                                    Text("Find", fontWeight = FontWeight.Bold)
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSlateSurface,
                                unfocusedContainerColor = DarkSlateSurface,
                                focusedBorderColor = HotRose,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (uiState.isSearchingYouTube) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = HotRose)
                            }
                        } else if (uiState.youtubeTracks.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                EmptyStateView(
                                    icon = Icons.Rounded.SmartDisplay,
                                    title = "YouTube to MP3 & Stream",
                                    subtitle = "Search any song, artist, or paste a YouTube watch link above to stream or download directly to your phone"
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Popular Suggestions:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                val popularQueries = listOf("Believer", "Shape of You", "Kesariya", "Arijit Singh", "Taylor Swift", "Despacito", "Alan Walker")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    popularQueries.forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(DarkSlateSurface)
                                                .clickable {
                                                    ytSearchInput = tag
                                                    onSearchYouTube(tag)
                                                }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                color = NeonCyan,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 120.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(
                                    items = uiState.youtubeTracks,
                                    key = { it.videoId }
                                ) { track ->
                                    YouTubeTrackItem(
                                        track = track,
                                        onStream = { onStreamYouTube(track) },
                                        onDownload = { onDownloadYouTube(track) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistListItem(playlist: Playlist) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Future: Open playlist tracks */ }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSlateSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                contentDescription = null,
                tint = ElectricViolet,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Custom Playlist",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun AlbumGridCard(album: Album) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSlateSurface)
            .clickable { /* Future: Open album */ }
            .padding(10.dp)
    ) {
        AsyncImage(
            model = album.artworkUri,
            contentDescription = album.name,
            placeholder = painterResource(id = R.drawable.ic_album_placeholder),
            error = painterResource(id = R.drawable.ic_album_placeholder),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = album.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${album.artist} • ${album.songCount} songs",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ArtistListItem(artist: Artist) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(DarkSlateSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = ElectricViolet,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "${artist.songCount} songs • ${artist.albumCount} albums",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}
