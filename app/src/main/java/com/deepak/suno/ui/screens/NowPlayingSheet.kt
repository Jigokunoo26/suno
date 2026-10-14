package com.deepak.suno.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.shadow
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
import com.deepak.suno.ui.theme.DarkSlateSurface
import com.deepak.suno.ui.theme.ElectricViolet
import com.deepak.suno.ui.theme.HotRose
import com.deepak.suno.ui.theme.MidnightObsidian
import com.deepak.suno.ui.theme.NeonCyan
import com.deepak.suno.ui.theme.TextPrimary
import com.deepak.suno.ui.theme.TextSecondary
import java.util.Locale

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
    onSetSleepTimer: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val song = playerState.currentSong ?: return

    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableFloatStateOf(0f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MidnightObsidian,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 2.sp
                )

                // Favorite Button in Header
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) HotRose else TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Album Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(1f)
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp), ambientColor = ElectricViolet)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSlateSurface),
                contentAlignment = Alignment.Center
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

            Spacer(modifier = Modifier.height(24.dp))

            // Song Title and Artist
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${song.artist} • ${song.album}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Seek Bar
            val currentSliderPosition = if (isUserScrubbing) {
                scrubPositionMs
            } else {
                playerState.currentPositionMs.toFloat()
            }
            val maxSliderRange = playerState.durationMs.toFloat().coerceAtLeast(1f)

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
                    thumbColor = ElectricViolet,
                    activeTrackColor = ElectricViolet,
                    inactiveTrackColor = ElectricViolet.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Time Labels (Elapsed and Duration)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(currentSliderPosition.toLong()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = formatMs(playerState.durationMs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls Row: Shuffle, Prev, Play/Pause, Next, Repeat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playerState.isShuffleEnabled) NeonCyan else TextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(onClick = onSkipPrevious) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = ElectricViolet,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(onClick = onSkipNext) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next Track",
                        tint = TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(onClick = onToggleRepeat) {
                    val repeatIcon = when (playerState.repeatMode) {
                        RepeatMode.ONE -> Icons.Rounded.RepeatOne
                        else -> Icons.Rounded.Repeat
                    }
                    val tint = when (playerState.repeatMode) {
                        RepeatMode.OFF -> TextSecondary
                        else -> NeonCyan
                    }
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        tint = tint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Secondary Quick Actions: Speed Chip & Sleep Timer Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Speed Toggle Chip
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
                    label = { Text("${playerState.playbackSpeed}x", color = TextPrimary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = "Speed",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = DarkSlateSurface)
                )

                Spacer(modifier = Modifier.width(16.dp))

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
                    label = {
                        val text = if (playerState.sleepTimerMinutesLeft != null) {
                            "${playerState.sleepTimerMinutesLeft}m left"
                        } else {
                            "Sleep Timer"
                        }
                        Text(text, color = TextPrimary)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Bedtime,
                            contentDescription = "Sleep Timer",
                            tint = if (playerState.sleepTimerMinutesLeft != null) NeonCyan else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = DarkSlateSurface)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatMs(milliseconds: Long): String {
    val totalSecs = (milliseconds / 1000).coerceAtLeast(0)
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format(Locale.getDefault(), "%d:%02d", mins, secs)
}
