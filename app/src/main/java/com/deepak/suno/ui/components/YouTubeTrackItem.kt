package com.deepak.suno.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.deepak.suno.domain.model.DownloadStatus
import com.deepak.suno.domain.model.YouTubeTrack
import com.deepak.suno.ui.theme.DarkSlateSurface
import com.deepak.suno.ui.theme.ElectricViolet
import com.deepak.suno.ui.theme.NeonCyan
import com.deepak.suno.ui.theme.TextPrimary
import com.deepak.suno.ui.theme.TextSecondary

@Composable
fun YouTubeTrackItem(
    track: YouTubeTrack,
    onStream: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSlateSurface)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with duration overlay
            Box(
                modifier = Modifier
                    .size(width = 84.dp, height = 56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                AsyncImage(
                    model = track.thumbnailUrl,
                    contentDescription = track.title,
                    placeholder = painterResource(id = R.drawable.ic_album_placeholder),
                    error = painterResource(id = R.drawable.ic_album_placeholder),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )

                // Duration badge on thumbnail
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = track.formattedDuration(),
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Channel
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.uploader,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons: Stream Now (Play) & Download
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Stream Button
                FilledIconButton(
                    onClick = onStream,
                    modifier = Modifier.size(38.dp),
                    enabled = !track.isStreamingLoading,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = NeonCyan.copy(alpha = 0.15f),
                        contentColor = NeonCyan
                    )
                ) {
                    if (track.isStreamingLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Stream Now",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Download Button / Status
                when (track.downloadStatus) {
                    DownloadStatus.IDLE, DownloadStatus.ERROR -> {
                        FilledIconButton(
                            onClick = onDownload,
                            modifier = Modifier.size(38.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (track.downloadStatus == DownloadStatus.ERROR)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                else
                                    ElectricViolet.copy(alpha = 0.2f),
                                contentColor = if (track.downloadStatus == DownloadStatus.ERROR)
                                    MaterialTheme.colorScheme.error
                                else
                                    ElectricViolet
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = "Download MP3",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    DownloadStatus.DOWNLOADING -> {
                        Box(
                            modifier = Modifier.size(38.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { track.downloadProgress },
                                modifier = Modifier.size(28.dp),
                                color = ElectricViolet,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "${(track.downloadProgress * 100).toInt()}%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                    DownloadStatus.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Downloaded to device",
                            tint = NeonCyan,
                            modifier = Modifier.size(32.dp).padding(4.dp)
                        )
                    }
                }
            }
        }

        // Linear progress bar if downloading
        if (track.downloadStatus == DownloadStatus.DOWNLOADING) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { track.downloadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = ElectricViolet,
                trackColor = ElectricViolet.copy(alpha = 0.2f)
            )
        }

        // Error message banner if download/stream failed
        if (!track.errorMessage.isNullOrBlank() && track.downloadStatus == DownloadStatus.ERROR) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Error: ${track.errorMessage}",
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
