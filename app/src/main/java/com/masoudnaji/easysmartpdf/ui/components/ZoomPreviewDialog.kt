package com.masoudnaji.easysmartpdf.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.domain.model.ThumbnailState
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@Composable
fun ZoomPreviewDialog(
    pages: List<PageItem>,
    initialPageId: String,
    onDismiss: () -> Unit
) {
    if (pages.isEmpty()) return

    val initialIndex = pages.indexOfFirst { it.id == initialPageId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = initialIndex) { pages.size }

    LaunchedEffect(initialPageId) {
        val idx = pages.indexOfFirst { it.id == initialPageId }
        if (idx >= 0 && idx != pagerState.currentPage) {
            pagerState.scrollToPage(idx)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val page = pages[pageIndex]
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (page.thumbnailState == ThumbnailState.Loaded) {
                        val bitmap = page.thumbnail
                        if (bitmap != null && !bitmap.isRecycled) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(Spacing.lg)
                                    .graphicsLayer { rotationZ = page.rotation.toFloat() }
                            )
                        }
                    }
                }
            }

            // Top bar overlay
            Column(modifier = Modifier.align(Alignment.TopCenter)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.page_editor_close_preview),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pages.getOrNull(pagerState.currentPage)?.sourceFileName ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(
                                R.string.page_editor_page_of,
                                pagerState.currentPage + 1,
                                pages.size
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
