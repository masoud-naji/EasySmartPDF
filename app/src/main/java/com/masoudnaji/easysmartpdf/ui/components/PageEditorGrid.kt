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
import androidx.compose.ui.platform.LocalDensity
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
    onMove: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Spacing.md)
) {
    val density = LocalDensity.current
    // Approximate card height in px for drag threshold calculation.
    // Card = A4 aspect (0.707) × (screen/2 - spacing) ≈ use 300dp as safe estimate.
    val cardHeightPx = with(density) { 300.dp.toPx() }
    val columnCount = 2

    // rememberUpdatedState lets the pointerInput block read latest list/size
    // without restarting (which would cancel an in-progress drag gesture).
    val currentPages by rememberUpdatedState(pages)

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
                    modifier = Modifier.pointerInput(itemKey) {
                        var trackedIdx = -1
                        var accumulatorY = 0f
                        detectDragGestures(
                            onDragStart = {
                                trackedIdx = currentPages.indexOfFirst { it.id == itemKey }
                                accumulatorY = 0f
                            },
                            onDragEnd = { trackedIdx = -1; accumulatorY = 0f },
                            onDragCancel = { trackedIdx = -1; accumulatorY = 0f },
                            onDrag = { _, delta ->
                                if (trackedIdx < 0) return@detectDragGestures
                                accumulatorY += delta.y
                                val size = currentPages.size
                                val threshold = cardHeightPx / 2f
                                if (accumulatorY > threshold && trackedIdx < size - 1) {
                                    // Dragging down — swap with next item in the same column
                                    // by advancing columnCount positions when possible,
                                    // otherwise advance by 1 to maintain expected feel.
                                    val target = (trackedIdx + columnCount).coerceAtMost(size - 1)
                                    onMove(trackedIdx, target)
                                    trackedIdx = target
                                    accumulatorY -= cardHeightPx
                                } else if (accumulatorY < -threshold && trackedIdx > 0) {
                                    val target = (trackedIdx - columnCount).coerceAtLeast(0)
                                    onMove(trackedIdx, target)
                                    trackedIdx = target
                                    accumulatorY += cardHeightPx
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}
