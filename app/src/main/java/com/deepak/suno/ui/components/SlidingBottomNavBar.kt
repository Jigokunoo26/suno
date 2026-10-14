package com.deepak.suno.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepak.suno.ui.screens.LibraryTab

/**
 * Sliding Navigation Bar adjusted to the bottom of the app.
 * Provides smooth horizontal sliding across all tabs:
 * Home, Tracks, Downloads, Favorites, Albums, Artists, Playlists, YouTube.
 * Includes official LIFE FLUX solutions private ltd watermark.
 */
@Composable
fun SlidingBottomNavBar(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
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
                        targetValue = if (isPressed) 0.93f else 1f,
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
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
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
                        tonalElevation = if (selected) 4.dp else 0.dp,
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

            // Subtle Corporate Watermark label below the pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
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
                        letterSpacing = 0.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
