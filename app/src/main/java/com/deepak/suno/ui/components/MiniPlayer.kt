package com.deepak.suno.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.deepak.suno.R
import com.deepak.suno.domain.model.PlayerState
import com.deepak.suno.ui.theme.GlassSurface
import com.deepak.suno.ui.theme.GlassTokens
import com.deepak.suno.ui.theme.MusicShapes
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Rhythm Expressive Floating MiniPlayer with frosted glassy finish.
 * Features:
 * 1. Frosted glass container with specular highlight border and ambient shadow.
 * 2. Morphing Play/Pause button (smooth circle to 9-sided rotating cookie shape).
 * 3. Horizontal track swipe gestures (swipe left for next, swipe right for previous).
 * 4. Animated accent progress bar at the top edge.
 * 5. Squircle album artwork with live pulsing PlayingEqIcon overlay.
 */
@Composable
fun MiniPlayer(
    playerState: PlayerState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSkipPrevious: () -> Unit = {}
) {
    val currentSong = playerState.currentSong ?: return

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Press scale effect
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "miniPlayerPressScale"
    )

    // Song change bounce animation
    var songChangeBounce by remember { mutableStateOf(false) }
    val bounceScale by animateFloatAsState(
        targetValue = if (songChangeBounce) 1.025f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceScale"
    )

    LaunchedEffect(currentSong.id) {
        songChangeBounce = true
        delay(120)
        songChangeBounce = false
    }

    // Horizontal swipe gesture for track skipping
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = dragOffsetX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dragOffset"
    )

    // Smooth progress bar animation
    val animatedProgress by animateFloatAsState(
        targetValue = playerState.progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "miniPlayerProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .scale(pressScale * bounceScale)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffsetX < -110f) {
                            onSkipNext()
                        } else if (dragOffsetX > 110f) {
                            onSkipPrevious()
                        }
                        dragOffsetX = 0f
                    },
                    onDragCancel = {
                        dragOffsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffsetX = (dragOffsetX + dragAmount).coerceIn(-180f, 180f)
                    }
                )
            }
    ) {
        // Frosted Glass container
        GlassSurface(
            shape = MusicShapes.MiniPlayerFloating,
            backgroundColor = GlassTokens.DarkGlassSurfaceHigh,
            borderColor = GlassTokens.GlassBorderDark,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Slim Accent Progress Bar with primary gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(3.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album Artwork Squircle with subtle glass border
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MusicShapes.AlbumCoverSmall)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    ) {
                        AsyncImage(
                            model = currentSong.artworkUri,
                            contentDescription = "MiniPlayer artwork",
                            placeholder = painterResource(id = R.drawable.ic_album_placeholder),
                            error = painterResource(id = R.drawable.ic_album_placeholder),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )

                        // Subtle equalizer animation when playing
                        if (playerState.isPlaying) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center
                            ) {
                                PlayingEqIcon(
                                    color = Color.White,
                                    isPlaying = true,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Track Title & Artist Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentSong.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentSong.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Skip Previous Button (Tonal glass)
                    FilledTonalIconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = GlassTokens.DarkGlassSurface,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous Track",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Rhythm Morphing Play/Pause Button (50dp)
                    MorphingPlayPauseButton(
                        isPlaying = playerState.isPlaying,
                        onClick = onPlayPause,
                        size = 46.dp,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        isBuffering = playerState.isBuffering
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Skip Next Button (Tonal glass)
                    FilledTonalIconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = GlassTokens.DarkGlassSurface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next Track",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
