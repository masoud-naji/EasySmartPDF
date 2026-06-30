package com.masoudnaji.easysmartpdf.ui.screens.merge

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import android.text.format.Formatter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.ui.components.AppCard
import com.masoudnaji.easysmartpdf.ui.components.PrimaryButton
import com.masoudnaji.easysmartpdf.ui.components.SecondaryButton
import com.masoudnaji.easysmartpdf.ui.theme.AppIconSize
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergePdfScreen(
    onBackClick: () -> Unit,
    onNavigateToProgress: () -> Unit,
    viewModel: MergePdfViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { viewModel.addPdf(it) }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage!!)
            viewModel.onErrorDismissed()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.merge_pdf_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                PrimaryButton(
                    text = stringResource(R.string.merge_action),
                    onClick = {
                        viewModel.startMerge()
                        onNavigateToProgress()
                    },
                    enabled = uiState.pdfList.size >= 2
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.screenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            AppCard {
                Text(
                    text = stringResource(R.string.merge_files_card_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                if (uiState.pdfList.isEmpty()) {
                    Text(
                        text = stringResource(R.string.merge_no_files_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                } else {
                    val count = uiState.pdfList.size
                    val totalPages = uiState.pdfList.sumOf { it.pageCount }
                    val totalSize = uiState.pdfList.sumOf { it.fileSize }
                    Text(
                        text = pluralStringResource(R.plurals.merge_summary_count, count, count),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val summaryMeta = buildString {
                        if (totalPages > 0) append("$totalPages pages")
                        if (totalPages > 0 && totalSize > 0) append(" • ")
                        if (totalSize > 0) append(Formatter.formatShortFileSize(context, totalSize))
                    }
                    if (summaryMeta.isNotEmpty()) {
                        Text(
                            text = summaryMeta,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    ReorderablePdfList(
                        pdfList = uiState.pdfList,
                        onRemove = { viewModel.removePdf(it) },
                        onMove = { from, to -> viewModel.movePdf(from, to) }
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }

                SecondaryButton(
                    text = if (uiState.pdfList.isEmpty())
                        stringResource(R.string.merge_add_pdf)
                    else
                        stringResource(R.string.merge_add_more_pdf),
                    onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                )
            }

            if (uiState.pdfList.size < 2) {
                Text(
                    text = stringResource(R.string.merge_need_two_files),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun ReorderablePdfList(
    pdfList: List<PdfEntry>,
    onRemove: (Int) -> Unit,
    onMove: (from: Int, to: Int) -> Unit
) {
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 56.dp.toPx() }
    // rememberUpdatedState gives the pointerInput coroutine the latest list and size
    // without restarting the block (which would cancel an in-progress drag).
    val currentList by rememberUpdatedState(pdfList)

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        pdfList.forEachIndexed { index, entry ->
            // key() gives each composable stable identity by URI instead of position.
            // Without it, swapping items restarts the pointerInput block at each slot,
            // cancelling the gesture after every single move.
            key(entry.uri.toString()) {
                val itemKey = entry.uri.toString()
                PdfListItem(
                    entry = entry,
                    onRemove = { onRemove(index) },
                    dragHandleModifier = Modifier.pointerInput(itemKey) {
                        // Local vars updated synchronously so multi-step drags work
                        // even when recomposition (updating currentList) lags a frame.
                        var trackedIdx = -1
                        var accumulator = 0f
                        detectDragGestures(
                            onDragStart = {
                                trackedIdx = currentList.indexOfFirst { it.uri.toString() == itemKey }
                                accumulator = 0f
                            },
                            onDragEnd = { trackedIdx = -1; accumulator = 0f },
                            onDragCancel = { trackedIdx = -1; accumulator = 0f },
                            onDrag = { _, delta ->
                                if (trackedIdx < 0) return@detectDragGestures
                                accumulator += delta.y
                                val size = currentList.size
                                val threshold = itemHeightPx / 2f
                                if (accumulator > threshold && trackedIdx < size - 1) {
                                    onMove(trackedIdx, trackedIdx + 1)
                                    trackedIdx++
                                    accumulator -= itemHeightPx
                                } else if (accumulator < -threshold && trackedIdx > 0) {
                                    onMove(trackedIdx, trackedIdx - 1)
                                    trackedIdx--
                                    accumulator += itemHeightPx
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PdfListItem(
    entry: PdfEntry,
    onRemove: () -> Unit,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val metaLine = buildString {
        if (entry.pageCount > 0) append("${entry.pageCount} pages")
        if (entry.pageCount > 0 && entry.fileSize > 0) append(" • ")
        if (entry.fileSize > 0) append(Formatter.formatShortFileSize(context, entry.fileSize))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = stringResource(R.string.merge_drag_handle),
            tint = MaterialTheme.colorScheme.outline,
            modifier = dragHandleModifier.size(AppIconSize.small)
        )

        Spacer(modifier = Modifier.size(Spacing.sm))

        Icon(
            imageVector = Icons.Default.PictureAsPdf,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(AppIconSize.small)
        )

        Spacer(modifier = Modifier.size(Spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.displayName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (entry.isLoadingMetadata) {
                Text(
                    text = stringResource(R.string.merge_loading_details),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else if (metaLine.isNotEmpty()) {
                Text(
                    text = metaLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.merge_remove_file),
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
