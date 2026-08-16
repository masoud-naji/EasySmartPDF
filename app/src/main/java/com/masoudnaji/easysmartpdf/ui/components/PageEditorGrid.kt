package com.masoudnaji.easysmartpdf.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@Composable
fun PageEditorGrid(
    pages: List<PageItem>,
    onRotateLeft: (pageId: String) -> Unit,
    onRotateRight: (pageId: String) -> Unit,
    onDelete: (pageId: String) -> Unit,
    onPageClick: (pageId: String) -> Unit,
    onLongClick: (pageId: String) -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Spacing.md)
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val columnCount = 2

    // Card width: screen width minus left+right content padding and the single
    // column gap, divided by column count. Uses Dp arithmetic to stay type-safe.
    val cardWidthPx = with(density) {
        val screenWidth = configuration.screenWidthDp.dp
        val occupied = Spacing.md * 2 + Spacing.sm
        ((screenWidth - occupied) / columnCount).toPx()
    }
    // Card height derived from A4 aspect ratio (width / 0.707).
    val cardHeightPx = cardWidthPx / 0.707f

    val currentPages by rememberUpdatedState(pages)

    // In RTL, LazyVerticalGrid places list[0] in the RIGHT column. We normalise
    // horizontal drag by negating delta.x so that "positive accX = towards higher
    // list index" holds in both layout directions.
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        itemsIndexed(
            items = pages,
            key = { _, page -> page.id }
        ) { _, page ->
            val itemKey = page.id
            key(itemKey) {
                PageThumbnailCard(
                    page = page,
                    pageNumber = currentPages.indexOfFirst { it.id == itemKey } + 1,
                    onRotateLeft = { onRotateLeft(itemKey) },
                    onRotateRight = { onRotateRight(itemKey) },
                    onDelete = { onDelete(itemKey) },
                    onClick = { onPageClick(itemKey) },
                    onLongClick = { onLongClick(itemKey) },
                    modifier = Modifier.pointerInput(itemKey, isRtl) {
                        var trackedIdx = -1
                        var accX = 0f
                        var accY = 0f
                        detectDragGestures(
                            onDragStart = {
                                trackedIdx = currentPages.indexOfFirst { it.id == itemKey }
                                accX = 0f
                                accY = 0f
                            },
                            onDragEnd = { trackedIdx = -1; accX = 0f; accY = 0f },
                            onDragCancel = { trackedIdx = -1; accX = 0f; accY = 0f },
                            onDrag = { _, delta ->
                                if (trackedIdx < 0) return@detectDragGestures
                                // Negate x in RTL so higher accX always means higher list index.
                                accX += if (isRtl) -delta.x else delta.x
                                accY += delta.y
                                val size = currentPages.size

                                // Only one axis fires per frame: vertical takes priority.
                                val vThreshold = cardHeightPx / 2f
                                val hThreshold = cardWidthPx / 2f
                                val col = trackedIdx % columnCount

                                if (accY > vThreshold && trackedIdx < size - 1) {
                                    val target = (trackedIdx + columnCount).coerceAtMost(size - 1)
                                    onMove(trackedIdx, target)
                                    trackedIdx = target
                                    accY -= cardHeightPx
                                } else if (accY < -vThreshold && trackedIdx > 0) {
                                    val target = (trackedIdx - columnCount).coerceAtLeast(0)
                                    onMove(trackedIdx, target)
                                    trackedIdx = target
                                    accY += cardHeightPx
                                } else if (accX > hThreshold) {
                                    if (col < columnCount - 1 && trackedIdx < size - 1) {
                                        onMove(trackedIdx, trackedIdx + 1)
                                        trackedIdx++
                                        accX -= cardWidthPx
                                    } else {
                                        accX = 0f
                                    }
                                } else if (accX < -hThreshold) {
                                    if (col > 0) {
                                        onMove(trackedIdx, trackedIdx - 1)
                                        trackedIdx--
                                        accX += cardWidthPx
                                    } else {
                                        accX = 0f
                                    }
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}
