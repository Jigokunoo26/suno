package com.deepak.suno.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import com.deepak.suno.domain.model.PlayerState
import com.deepak.suno.domain.model.RepeatMode
import com.deepak.suno.ui.components.MorphingPlayPauseButton
import com.deepak.suno.ui.components.PlayingEqIcon
import com.deepak.suno.ui.theme.AmbientAuraGlow
import com.deepak.suno.ui.theme.GlassTokens
import com.deepak.suno.ui.theme.HotRose
import com.deepak.suno.ui.theme.MusicShapes
import java.util.Locale

/**
 * Suno Signature Now Playing Screen:
 * Features:
 * 1. Opaque, deep obsidian background with frosted glassy finish ensuring ZERO bleed-through.
 * 2. MorphingPlayPauseButton (74dp) rolling/rotating centerpiece widget that morphs and rotates smoothly during active playback.
 * 3. Specular glassmorphic album artwork card with ambient pulsating aura glow.
 * 4. Direct Sleep Timer & Playback Speed Quick Action chips right on the main player screen.
 * 5. Full controls deck: Shuffle, Skip Previous, Rolling Play/Pause widget, Skip Next, Repeat.
 * 6. Interactive Up Next / Playback Queue drawer sheet.
 * 7. Verified corporate watermark: "LIFE FLUX solutions private ltd".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet(
    playerState: PlayerState,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onSkipToQueueItem: (Int) -> Unit = {},
    onRemoveQueueItem: (Int) -> Unit = {},
    onClearQueue: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val song = playerState.currentSong ?: return

    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableFloatStateOf(0f) }
    var showQueueSheet by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = MusicShapes.PlayerCard,
        containerColor = Color(0xFF0D0C14),
        scrimColor = Color.Black.copy(alpha = 0.70f),
        dragHandle = null
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0C14))
                .navigationBarsPadding()
        ) {
            val isTallDevice = maxHeight >= 680.dp
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (!isTallDevice) Modifier.verticalScroll(scrollState) else Modifier)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (isTallDevice) Arrangement.SpaceBetween else Arrangement.spacedBy(14.dp)
            ) {
                // Drag Handle Pill
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 4.dp)
                        .size(width = 42.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(GlassTokens.GlassBorderDark)
                )

                // Top Header: Dismiss, Title & Source badge, Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(42.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = GlassTokens.DarkGlassSurfaceHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = CircleShape,
                            color = GlassTokens.DarkGlassSurfaceHigh,
                            border = BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = if (song.isDownloaded) "OFFLINE HQ" else "STUDIO AUDIO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Favorite Button with Bouncy scale
                    FilledTonalIconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(42.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = GlassTokens.DarkGlassSurfaceHigh,
                            contentColor = if (isFavorite) HotRose else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        val favScale by animateFloatAsState(
                            targetValue = if (isFavorite) 1.25f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "favScale"
                        )
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite",
                            modifier = Modifier
                                .size(22.dp)
                                .scale(favScale)
                        )
                    }
                }

                if (!isTallDevice) Spacer(modifier = Modifier.height(10.dp))

                // Large Album Artwork with Ambient Glow Aura and Glass Card Border
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isTallDevice) 0.86f else 0.72f)
                        .sizeIn(maxHeight = if (isTallDevice) 320.dp else 230.dp)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Ambient pulsating glow layer
                    AmbientAuraGlow(
                        modifier = Modifier.fillMaxSize(0.96f),
                        blurRadius = 32.dp
                    )

                    // Foreground Artwork Card with Glassmorphic Border
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = GlassTokens.DarkGlassSurfaceHigh,
                        shadowElevation = 18.dp,
                        tonalElevation = 6.dp,
                        border = BorderStroke(
                            1.5.dp,
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.32f),
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.16f)
                                )
                            )
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AsyncImage(
                            model = song.artworkUri,
                            contentDescription = "Album Art",
                            placeholder = painterResource(id = R.drawable.ic_album_placeholder),
                            error = painterResource(id = R.drawable.ic_album_placeholder),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (!isTallDevice) Spacer(modifier = Modifier.height(14.dp))

                // Song Title and Artist Metadata
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (song.album.isNotBlank() && song.album != song.title) "${song.artist} • ${song.album}" else song.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isTallDevice) Spacer(modifier = Modifier.height(10.dp))

                // Scrubber / Slider with elapsed and total duration
                val currentSliderPosition = if (isUserScrubbing) {
                    scrubPositionMs
                } else {
                    playerState.currentPositionMs.toFloat()
                }
                val maxSliderRange = playerState.durationMs.toFloat().coerceAtLeast(1f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Slider(
                        value = currentSliderPosition.coerceIn(0f, maxSliderRange),
                        onValueChange = {
                            isUserScrubbing = true
                            scrubPositionMs = it
                        },
                        onValueChangeFinished = {
                            onSeekTo(scrubPositionMs.toLong())
                            isUserScrubbing = false
                        },
                        valueRange = 0f..maxSliderRange,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.40f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Timestamps (Elapsed & Total duration)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatMs(currentSliderPosition.toLong()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatMs(playerState.durationMs),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (!isTallDevice) Spacer(modifier = Modifier.height(12.dp))

                // Controls Deck: Shuffle, Prev, Rolling Play/Pause Morphing Widget (74dp), Next, Repeat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle Button with Active Indicator
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onToggleShuffle,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (playerState.isShuffleEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (playerState.isShuffleEnabled) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    // Skip Previous Button (Tonal Glass)
                    FilledTonalIconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = GlassTokens.DarkGlassSurfaceHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous Track",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Rolling Play / Pause Widget (Morphing cookie rolling during playback)
                    MorphingPlayPauseButton(
                        isPlaying = playerState.isPlaying,
                        onClick = onPlayPause,
                        size = 74.dp,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        isBuffering = playerState.isBuffering
                    )

                    // Skip Next Button (Tonal Glass)
                    FilledTonalIconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = GlassTokens.DarkGlassSurfaceHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next Track",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Repeat Mode Button with Cycle State Indicator
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onToggleRepeat,
                            modifier = Modifier.size(46.dp)
                        ) {
                            val repeatIcon = when (playerState.repeatMode) {
                                RepeatMode.ONE -> Icons.Rounded.RepeatOne
                                else -> Icons.Rounded.Repeat
                            }
                            val tint = when (playerState.repeatMode) {
                                RepeatMode.OFF -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Icon(
                                imageVector = repeatIcon,
                                contentDescription = "Repeat",
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (playerState.repeatMode != RepeatMode.OFF) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                if (!isTallDevice) Spacer(modifier = Modifier.height(12.dp))

                // Direct Quick Actions: Speed & Sleep Timer Chips with Glassmorphic styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Playback Speed Chip
                    AssistChip(
                        onClick = {
                            val nextSpeed = when (playerState.playbackSpeed) {
                                1.0f -> 1.25f
                                1.25f -> 1.5f
                                1.5f -> 0.75f
                                else -> 1.0f
                            }
                            onSetPlaybackSpeed(nextSpeed)
                        },
                        shape = CircleShape,
                        label = {
                            Text(
                                text = "${playerState.playbackSpeed}x Speed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = "Playback Speed",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = GlassTokens.DarkGlassSurfaceHigh
                        ),
                        border = BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    // Sleep Timer Chip
                    AssistChip(
                        onClick = {
                            val nextTimer = when (playerState.sleepTimerMinutesLeft) {
                                null -> 15
                                15 -> 30
                                30 -> 60
                                else -> 0
                            }
                            onSetSleepTimer(nextTimer)
                        },
                        shape = CircleShape,
                        label = {
                            val text = if (playerState.sleepTimerMinutesLeft != null) {
                                "${playerState.sleepTimerMinutesLeft}m left"
                            } else {
                                "Sleep Timer"
                            }
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Bedtime,
                                contentDescription = "Sleep Timer",
                                tint = if (playerState.sleepTimerMinutesLeft != null) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = GlassTokens.DarkGlassSurfaceHigh
                        ),
                        border = BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark)
                    )
                }

                // Device Output & Queue Action Bar (Adjusted directly to the device bottom)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Audio Output Device Indicator
                    Surface(
                        shape = CircleShape,
                        color = GlassTokens.DarkGlassSurfaceHigh,
                        border = BorderStroke(1.dp, GlassTokens.GlassBorderSubtleDark),
                        modifier = Modifier.clip(CircleShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Speaker,
                                contentDescription = "Speaker",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Phone speaker",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    // Queue button
                    FilledTonalIconButton(
                        onClick = { showQueueSheet = true },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = GlassTokens.DarkGlassSurfaceHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.QueueMusic,
                            contentDescription = "Queue",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Official Corporate Watermark in player
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "LIFE FLUX solutions private ltd",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.8.sp,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // --- Interactive Up Next / Playback Queue Sheet ---
        if (showQueueSheet) {
            val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = queueSheetState,
                shape = MusicShapes.PlayerCard,
                containerColor = Color(0xFF14131D),
                scrimColor = Color.Black.copy(alpha = 0.70f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF14131D))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Playback Queue",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${playerState.currentQueue.size} songs • Continuous Autoplay",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { showQueueSheet = false },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = GlassTokens.DarkGlassSurfaceHigh
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Queue",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (playerState.currentQueue.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Queue is currently empty",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(playerState.currentQueue) { index, queueSong ->
                                val isCurrent = index == playerState.currentQueueIndex

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else GlassTokens.DarkGlassSurfaceHigh,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCurrent) MaterialTheme.colorScheme.primary else GlassTokens.GlassBorderSubtleDark
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSkipToQueueItem(index)
                                            showQueueSheet = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Index or Playing Equalizer
                                        Box(
                                            modifier = Modifier.size(28.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isCurrent && playerState.isPlaying) {
                                                PlayingEqIcon(
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    isPlaying = true,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = "${index + 1}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Mini Artwork
                                        AsyncImage(
                                            model = queueSong.artworkUri,
                                            contentDescription = null,
                                            placeholder = painterResource(id = R.drawable.ic_album_placeholder),
                                            error = painterResource(id = R.drawable.ic_album_placeholder),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Song Details
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = queueSong.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = queueSong.artist,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Remove Item from queue
                                        IconButton(
                                            onClick = { onRemoveQueueItem(index) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
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
    }
}

private fun formatMs(milliseconds: Long): String {
    val totalSecs = (milliseconds / 1000).coerceAtLeast(0)
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format(Locale.getDefault(), "%d:%02d", mins, secs)
}
