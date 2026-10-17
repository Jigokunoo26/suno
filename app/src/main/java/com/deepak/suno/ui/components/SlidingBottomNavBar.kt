package com.deepak.suno.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepak.suno.ui.screens.LibraryTab
import com.deepak.suno.ui.theme.GlassTokens
import com.deepak.suno.ui.theme.MusicShapes

/**
 * Rhythm Expressive Floating Glass Navigation Dock for Suno.
 * Features:
 * 1. Frosted glass surface dock hovering above the navigation bar.
 * 2. Specular glass edge highlights and smooth horizontal sliding across tabs:
 *    Home, Tracks, Downloads, Favorites, Albums, Artists, Playlists, YouTube.
 * 3. Vibrant gradient pill indicators for the active selection.
 * 4. Corporate watermark badge: LIFE FLUX solutions private ltd.
 */
@Composable
fun SlidingBottomNavBar(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = GlassTokens.DarkGlassBackground,
        shadowElevation = 14.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    GlassTokens.GlassBorderDark,
                    Color.Transparent
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LibraryTab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    val tabIcon = when (tab) {
                        LibraryTab.HOME -> Icons.Rounded.Home
                        LibraryTab.SONGS -> Icons.Rounded.MusicNote
                        LibraryTab.DOWNLOADS -> Icons.Rounded.DownloadDone
                        LibraryTab.FAVORITES -> Icons.Rounded.Favorite
                        LibraryTab.ALBUMS -> Icons.Rounded.Album
                        LibraryTab.ARTISTS -> Icons.Rounded.Person
                        LibraryTab.PLAYLISTS -> Icons.AutoMirrored.Rounded.QueueMusic
                        LibraryTab.YOUTUBE -> Icons.Rounded.SmartDisplay
                    }

                    val tabTitle = when (tab) {
                        LibraryTab.HOME -> "Home"
                        LibraryTab.SONGS -> "Tracks"
                        LibraryTab.DOWNLOADS -> "Downloads"
                        LibraryTab.FAVORITES -> "Favorites"
                        LibraryTab.ALBUMS -> "Albums"
                        LibraryTab.ARTISTS -> "Artists"
                        LibraryTab.PLAYLISTS -> "Playlists"
                        LibraryTab.YOUTUBE -> "YouTube"
                    }

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val chipScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.92f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "chipScale"
                    )

                    val backgroundColor by animateColorAsState(
                        targetValue = if (selected) {
                            when (tab) {
                                LibraryTab.YOUTUBE -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        } else {
                            GlassTokens.DarkGlassSurface.copy(alpha = 0.6f)
                        },
                        label = "chipBg"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (selected) {
                            when (tab) {
                                LibraryTab.YOUTUBE -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        label = "chipContent"
                    )

                    Surface(
                        onClick = { onTabSelected(tab) },
                        interactionSource = interactionSource,
                        shape = CircleShape,
                        color = backgroundColor,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            } else {
                                GlassTokens.GlassBorderSubtleDark
                            }
                        ),
                        tonalElevation = if (selected) 6.dp else 0.dp,
                        modifier = Modifier.scale(chipScale)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tabIcon,
                                contentDescription = tabTitle,
                                tint = contentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tabTitle,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor
                            )
                        }
                    }
                }
            }

            // Official Corporate Watermark
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "LIFE FLUX solutions private ltd",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 0.6.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
