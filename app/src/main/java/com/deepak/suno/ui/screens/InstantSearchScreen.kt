package com.deepak.suno.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepak.suno.domain.model.Song
import com.deepak.suno.domain.model.YouTubeTrack
import com.deepak.suno.ui.components.SongListItem
import com.deepak.suno.ui.components.YouTubeTrackItem
import com.deepak.suno.ui.theme.GlassTokens

/**
 * Rhythm's signature Instant Search Screen (matching Screen 6 in screenshot).
 * Features:
 * - Large search squircle icon with "Search across Local & Streaming".
 * - "Browse by Genre" 6 vibrant squircle genre cards:
 *   Alternative Rock, Anime, Bollywood, Electronic, Film & Animation, Hip-Hop.
 * - Floating bottom search bar with category pills: Songs, Albums, Artists, Playlists.
 * - Immediate live stream and download results.
 */
data class RhythmGenre(
    val title: String,
    val songCountLabel: String,
    val color: Color,
    val query: String
)

@Composable
fun InstantSearchScreen(
    uiState: LibraryUiState,
    onSearchYouTube: (String) -> Unit,
    onStreamYouTube: (YouTubeTrack) -> Unit,
    onDownloadYouTube: (YouTubeTrack) -> Unit,
    onSongClicked: (Song) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf(uiState.searchQuery) }
    var selectedCategory by remember { mutableStateOf("Songs") }

    val categories = listOf("Songs", "Albums", "Artists", "Playlists")

    val genres = remember {
        listOf(
            RhythmGenre("Telugu Hits", "Tollywood & Chartbusters", Color(0xFFE11D48), "Telugu hits songs"),
            RhythmGenre("Hindi Bollywood", "Top Chartbusters & Party", Color(0xFFD97706), "Hindi Bollywood hits"),
            RhythmGenre("Tamil Melodies", "Kollywood Beats", Color(0xFF2563EB), "Tamil songs hits"),
            RhythmGenre("English & Global", "Pop, Rock & Billboard", Color(0xFF7C3AED), "English pop hits"),
            RhythmGenre("Malayalam Wave", "Sandalwood & Mollywood", Color(0xFF059669), "Malayalam hits songs"),
            RhythmGenre("Punjabi Beats", "Bhangra & Desi Hip Hop", Color(0xFFEA580C), "Punjabi hits songs")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 140.dp) // Space for bottom floating search bar
        ) {
            // Top Header with Back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onBack,
                    modifier = Modifier.size(42.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = GlassTokens.DarkGlassSurfaceHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Search",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // If user hasn't typed anything yet, show the "Browse by Genre" Rhythm screen!
            if (searchInput.isBlank() && uiState.youtubeTracks.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Large Center Squircle Search Icon
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = GlassTokens.DarkGlassSurfaceHigh,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderDark),
                        modifier = Modifier.size(86.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Search across Local & Streaming",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // "Browse by Genre" Header with sparkle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ Browse by Genre",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Grid of 6 Genre Tiles (matching Screen 6)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(genres) { genre ->
                            GenreTile(
                                genre = genre,
                                onClick = {
                                    searchInput = genre.title
                                    onSearchYouTube(genre.query)
                                }
                            )
                        }
                    }
                }
            } else {
                // Search Results View (Streaming & Local)
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.isSearchingYouTube) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // YouTube Streaming Tracks
                    if (uiState.youtubeTracks.isNotEmpty()) {
                        item {
                            Text(
                                text = "STREAMING MUSIC",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }

                        items(uiState.youtubeTracks) { track ->
                            YouTubeTrackItem(
                                track = track,
                                onStream = { onStreamYouTube(track) },
                                onDownload = { onDownloadYouTube(track) }
                            )
                        }
                    }

                    // Matching Local Songs
                    val matchingLocal = uiState.songs.filter {
                        it.title.contains(searchInput, ignoreCase = true) ||
                        it.artist.contains(searchInput, ignoreCase = true) ||
                        it.album.contains(searchInput, ignoreCase = true)
                    }

                    if (matchingLocal.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "LOCAL TRACKS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }

                        items(matchingLocal) { song ->
                            val isCurrentSong = uiState.playerState.currentSong?.id == song.id
                            SongListItem(
                                song = song,
                                isCurrentSong = isCurrentSong,
                                isPlaying = isCurrentSong && uiState.playerState.isPlaying,
                                isFavorite = uiState.favorites.contains(song.id),
                                onToggleFavorite = { onToggleFavorite(song.id) },
                                onClick = { onSongClicked(song) }
                            )
                        }
                    }
                }
            }
        }

        // Floating Search Bar Dock at Bottom (matching Screen 6 in screenshot)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(28.dp),
            color = GlassTokens.DarkGlassBackground,
            shadowElevation = 18.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassTokens.GlassBorderDark)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                // Category Pills (Songs, Albums, Artists, Playlists)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            onClick = {
                                selectedCategory = category
                                if (category == "Albums" && searchInput.isNotBlank() && !searchInput.contains("album", ignoreCase = true)) {
                                    onSearchYouTube("$searchInput album")
                                } else if (category == "Songs" && searchInput.isNotBlank()) {
                                    onSearchYouTube(searchInput.replace(Regex("(?i)\\s*album"), ""))
                                }
                            },
                            shape = CircleShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else GlassTokens.DarkGlassSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else GlassTokens.GlassBorderSubtleDark
                            )
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input Field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(GlassTokens.DarkGlassSurfaceHigh)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = {
                            searchInput = it
                            onSearchYouTube(it)
                        },
                        placeholder = {
                            Text(
                                text = "Search everywhere...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    if (searchInput.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchInput = ""
                                onSearchYouTube("")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Genre tile matching Screen 6 of Rhythm.
 */
@Composable
private fun GenreTile(
    genre: RhythmGenre,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "genreScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        color = genre.color,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .scale(scale)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = genre.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = genre.songCountLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}
