package com.deepak.suno.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deepak.suno.R
import com.deepak.suno.domain.model.Song
import com.deepak.suno.domain.model.YouTubeTrack
import com.deepak.suno.ui.components.PlayingEqIcon
import com.deepak.suno.ui.theme.GlassTokens

import androidx.compose.material3.CircularProgressIndicator
import com.deepak.suno.ui.components.YouTubeTrackItem

/**
 * Suno Music Player - Home Screen (Integrated with Rhythm Frontend UI).
 * Features:
 * 1. Official Suno Branding & LIFE FLUX solutions private ltd verification.
 * 2. Instant YouTube Online Streaming search bar with in-place streaming results.
 * 3. Rhythm Featured Hero Spotlight Card with artwork, Play pill, and metadata.
 * 4. Rhythm "Recently Played" section with dual action pills: [ Play ] and [ 🔀 Shuffle ].
 * 5. Indian Regional Genres (Telugu, Hindi, English, Tamil, Malayalam, Kannada, Punjabi, Devotional).
 * 6. Offline Vault & Downloads quick access card.
 * 7. Top Artists & Non-blocking storage permission card.
 */
@Composable
fun HomeScreen(
    uiState: LibraryUiState,
    hasAudioPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSongClicked: (Song) -> Unit,
    onPlayQueue: (List<Song>, Int) -> Unit,
    onNavigateToTab: (LibraryTab) -> Unit,
    onSearchYouTube: (String) -> Unit,
    onStreamYouTube: (YouTubeTrack) -> Unit,
    onDownloadYouTube: (YouTubeTrack) -> Unit = {},
    onPlayAllYouTube: (List<YouTubeTrack>) -> Unit = {},
    onClearYouTubeResults: () -> Unit = {},
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit = {},
    onOpenArtist: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPermissionBannerDismissed by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var onlineSearchInput by remember { mutableStateOf("") }

    // Featured Spotlight track or album
    val featuredSong = uiState.playerState.currentSong
        ?: uiState.recentlyPlayedSongs.firstOrNull()
        ?: uiState.songs.firstOrNull()

    val recentlyPlayed = uiState.recentlyPlayedSongs

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 195.dp) // Space for bottom dock & miniplayer
    ) {
        // --- 1. Suno Top App Bar: Avatar + Verified Suno Branding + Actions ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Musical Equalizer Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.playerState.isPlaying) {
                        PlayingEqIcon(
                            isPlaying = true,
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = "Suno",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Suno",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verified Badge",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "LIFE FLUX solutions private ltd",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Right Actions: Direct YouTube Online Stream + Refresh + About
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Direct YouTube Online Stream Icon
                FilledTonalIconButton(
                    onClick = { onNavigateToTab(LibraryTab.YOUTUBE) },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SmartDisplay,
                        contentDescription = "Stream Online on YouTube",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Refresh Library
                FilledTonalIconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = GlassTokens.DarkGlassSurfaceHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // About Info
                FilledTonalIconButton(
                    onClick = { showAboutDialog = true },
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = GlassTokens.DarkGlassSurfaceHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "About",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // --- 2. YouTube Online Streaming Direct Search Bar ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(22.dp),
            color = GlassTokens.DarkGlassSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.SmartDisplay,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Stream Online • YouTube Music",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = onlineSearchInput,
                    onValueChange = { onlineSearchInput = it },
                    placeholder = {
                        Text(
                            text = "Search song, movie, artist to stream online...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Button(
                            onClick = {
                                if (onlineSearchInput.isNotBlank()) {
                                    onSearchYouTube(onlineSearchInput.trim())
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = MaterialTheme.colorScheme.onTertiary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Stream", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GlassTokens.DarkGlassSurfaceHigh,
                        unfocusedContainerColor = GlassTokens.DarkGlassSurfaceHigh,
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Trending YouTube Quick Pick Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val trendingTags = listOf(
                        "Telugu Hits 2026",
                        "Chirutha Chamka Chamka",
                        "Trending Bollywood",
                        "Lo-Fi Beats Chill",
                        "English Pop Top 50",
                        "Desi Hip-Hop",
                        "Arijit Singh Hits",
                        "Sid Sriram Hits"
                    )

                    trendingTags.forEach { tag ->
                        Surface(
                            onClick = {
                                onlineSearchInput = tag
                                onSearchYouTube(tag)
                            },
                            shape = CircleShape,
                            color = GlassTokens.DarkGlassSurfaceHighest,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2.1 In-Place YouTube Online Search Results directly on Home Screen ---
        if (uiState.isSearchingYouTube) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                color = GlassTokens.DarkGlassSurfaceHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.tertiary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Searching online music streams...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else if (uiState.youtubeTracks.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(22.dp),
                color = GlassTokens.DarkGlassSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Online Stream Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.youtubeTracks.size} tracks • Tap to stream or download",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onPlayAllYouTube(uiState.youtubeTracks) },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = {
                                    onlineSearchInput = ""
                                    onClearYouTubeResults()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Dismiss online results",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.youtubeTracks.take(8).forEach { track ->
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

        Spacer(modifier = Modifier.height(14.dp))

        // --- 3. Non-blocking Storage Permission Card ---
        if (!hasAudioPermission && !isPermissionBannerDismissed) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GlassTokens.DarkGlassSurfaceHigh
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LockOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Grant Storage Permission",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Load all your offline audio tracks and albums",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onRequestPermission,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Grant", fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = { isPermissionBannerDismissed = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // --- 4. Rhythm Featured Hero Spotlight Card ---
        if (featuredSong != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(290.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .clickable { onSongClicked(featuredSong) }
            ) {
                AsyncImage(
                    model = featuredSong.artworkUri,
                    contentDescription = featuredSong.title,
                    placeholder = painterResource(id = R.drawable.ic_album_placeholder),
                    error = painterResource(id = R.drawable.ic_album_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Scrim Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.85f),
                                    Color.Black.copy(alpha = 0.95f)
                                ),
                                startY = 100f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = featuredSong.album.ifBlank { featuredSong.title },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = featuredSong.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onSongClicked(featuredSong) },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6B4EFF),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Play",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (featuredSong.isDownloaded) "Offline HQ" else "Studio Audio",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- 5. "Recently Played" Section with Action Pills ---
        if (recentlyPlayed.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recently Played",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Continue your musical journey",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { onPlayQueue(recentlyPlayed, 0) },
                        shape = CircleShape,
                        color = GlassTokens.DarkGlassSurfaceHigh,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Play",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        onClick = { onPlayQueue(recentlyPlayed.shuffled(), 0) },
                        shape = CircleShape,
                        color = GlassTokens.DarkGlassSurfaceHigh,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recentlyPlayed.take(12)) { song ->
                    Surface(
                        onClick = { onSongClicked(song) },
                        shape = RoundedCornerShape(18.dp),
                        color = GlassTokens.DarkGlassSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark),
                        modifier = Modifier.width(180.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassTokens.DarkGlassSurfaceHighest)
                            ) {
                                AsyncImage(
                                    model = song.artworkUri,
                                    contentDescription = null,
                                    placeholder = painterResource(id = R.drawable.ic_album_placeholder),
                                    error = painterResource(id = R.drawable.ic_album_placeholder),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artist,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- 6. "Browse by Genre" 6 Vibrant Squircles (Streams on YouTube) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Browse by Genre",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Online Stream",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val indianGenres = remember {
            listOf(
                Triple("Telugu", "Tollywood Hits", Color(0xFFE65100)),
                Triple("Hindi", "Bollywood & Pop", Color(0xFFC62828)),
                Triple("English", "Indian Top Hits", Color(0xFF1565C0)),
                Triple("Tamil", "Kollywood Melodies", Color(0xFFAD1457)),
                Triple("Malayalam", "Mollywood Hits", Color(0xFF2E7D32)),
                Triple("Kannada", "Sandalwood Hits", Color(0xFFD84315)),
                Triple("Punjabi", "Bhangra & Pop", Color(0xFF6A1B9A)),
                Triple("Devotional", "Carnatic & Bhakti", Color(0xFF00695C))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            indianGenres.chunked(2).forEach { rowPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowPair.forEach { (genreTitle, genreSubtitle, genreColor) ->
                        Surface(
                            onClick = {
                                onlineSearchInput = "$genreTitle songs"
                                onSearchYouTube("$genreTitle songs")
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = genreColor.copy(alpha = 0.88f),
                            modifier = Modifier
                                .weight(1f)
                                .height(78.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = genreTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.TopStart)
                                )

                                Text(
                                    text = genreSubtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.align(Alignment.BottomStart)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 7. Offline Vault & Downloads Quick Card ---
        val downloadedSongs = uiState.downloadedSongs
        if (downloadedSongs.isNotEmpty()) {
            Surface(
                onClick = { onNavigateToTab(LibraryTab.DOWNLOADS) },
                shape = RoundedCornerShape(22.dp),
                color = GlassTokens.DarkGlassSurfaceHigh,
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF006450)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DownloadDone,
                            contentDescription = null,
                            tint = Color(0xFF2AE8B8),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Offline Vault",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${downloadedSongs.size} offline tracks stored on device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { onPlayQueue(downloadedSongs, 0) },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF006450),
                            contentColor = Color(0xFF2AE8B8)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- 8. Top Artists Section ---
        if (uiState.artists.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Artists",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToTab(LibraryTab.ARTISTS) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.artists.take(8)) { artist ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(88.dp)
                            .clickable { onOpenArtist(artist.name) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = artist.name.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = artist.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
        }

        // --- 9. Corporate Watermark & Verification ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clickable { showAboutDialog = true },
            shape = RoundedCornerShape(22.dp),
            color = GlassTokens.DarkGlassSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = "Verified Badge",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIFE FLUX solutions private ltd",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Official Release • Suno Music Player",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Corporate About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = GlassTokens.DarkGlassBackground,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "About Suno",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Suno Music Player",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Developed by LIFE FLUX solutions private ltd.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Features high-fidelity YouTube streaming, continuous queue playback, offline music vault, dynamic waveforms, and glassy UI controls.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
