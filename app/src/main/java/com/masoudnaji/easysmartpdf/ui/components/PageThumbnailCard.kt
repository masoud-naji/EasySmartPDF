package com.masoudnaji.easysmartpdf.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.domain.model.ThumbnailState
import com.masoudnaji.easysmartpdf.ui.theme.Radius
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@Composable
fun PageThumbnailCard(
    page: PageItem,
    pageNumber: Int,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.sm),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Thumbnail area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.707f) // A4 portrait ratio
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                when (page.thumbnailState) {
                    ThumbnailState.Loaded -> {
                        val bitmap = page.thumbnail
                        if (bitmap != null && !bitmap.isRecycled) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationZ = page.rotation.toFloat() }
                            )
                        }
                    }
                    ThumbnailState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.errorContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.page_editor_thumbnail_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                // Page number badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(Spacing.xs)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$pageNumber",
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.xs)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.page_editor_delete_page),
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Source file name
            Text(
                text = page.sourceFileName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xs)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Rotate actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xs),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onRotateLeft, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                        contentDescription = stringResource(R.string.page_editor_rotate_left),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onRotateRight, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = stringResource(R.string.page_editor_rotate_right),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
