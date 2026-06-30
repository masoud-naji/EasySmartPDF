package com.masoudnaji.easysmartpdf.ui.screens.imagetopdf

import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.domain.model.FitMode
import com.masoudnaji.easysmartpdf.domain.model.ImageEntry
import com.masoudnaji.easysmartpdf.domain.model.PdfOutputQuality
import com.masoudnaji.easysmartpdf.domain.model.PdfMargin
import com.masoudnaji.easysmartpdf.domain.model.PdfOrientation
import com.masoudnaji.easysmartpdf.domain.model.PdfPageSize
import com.masoudnaji.easysmartpdf.ui.components.AppCard
import com.masoudnaji.easysmartpdf.ui.components.PrimaryButton
import com.masoudnaji.easysmartpdf.ui.components.SecondaryButton
import com.masoudnaji.easysmartpdf.ui.theme.AppIconSize
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToPdfScreen(
    onBackClick: () -> Unit,
    onNavigateToProgress: () -> Unit,
    viewModel: ImageToPdfViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val thumbnails by viewModel.thumbnails.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        val msg = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.onErrorDismissed()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> uris.forEach { viewModel.addImage(it) } }

    val isCreating = uiState.createState !is ImageToPdfState.Idle
        && uiState.createState !is ImageToPdfState.Cancelled

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.image_to_pdf_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PrimaryButton(
                text = stringResource(R.string.image_to_pdf_action),
                onClick = {
                    viewModel.startCreation()
                    onNavigateToProgress()
                },
                enabled = uiState.imageList.isNotEmpty() && !isCreating,
                modifier = Modifier.padding(Spacing.lg)
            )
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

            // Card 1: Images
            AppCard {
                Text(
                    text = stringResource(R.string.image_to_pdf_images_card_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(Spacing.sm))

                if (uiState.imageList.isEmpty()) {
                    Text(
                        text = stringResource(R.string.image_to_pdf_no_images_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                } else {
                    val count = uiState.imageList.size
                    val totalSize = uiState.imageList.sumOf { it.fileSize }
                    Text(
                        text = pluralStringResource(R.plurals.image_to_pdf_summary_count, count, count),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (totalSize > 0L) {
                        Text(
                            text = Formatter.formatShortFileSize(context, totalSize),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    ReorderableImageList(
                        imageList = uiState.imageList,
                        thumbnails = thumbnails,
                        onRemove = { viewModel.removeImage(it) },
                        onMove = { from, to -> viewModel.moveImage(from, to) }
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }

                SecondaryButton(
                    text = if (uiState.imageList.isEmpty())
                        stringResource(R.string.image_to_pdf_add_images)
                    else
                        stringResource(R.string.image_to_pdf_add_more_images),
                    onClick = {
                        imagePickerLauncher.launch(arrayOf("image/jpeg", "image/png", "image/webp"))
                    },
                    enabled = !isCreating
                )
            }

            // Card 2: PDF Options
            AppCard {
                Text(
                    text = stringResource(R.string.image_to_pdf_options_card_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(Spacing.sm))

                OptionSection(label = stringResource(R.string.image_to_pdf_page_size_label)) {
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_a4),
                        selected = uiState.pageSize == PdfPageSize.A4,
                        onClick = { viewModel.onPageSizeChanged(PdfPageSize.A4) }
                    )
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_letter),
                        selected = uiState.pageSize == PdfPageSize.LETTER,
                        onClick = { viewModel.onPageSizeChanged(PdfPageSize.LETTER) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

                OptionSection(label = stringResource(R.string.image_to_pdf_orientation_label)) {
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_portrait),
                        selected = uiState.orientation == PdfOrientation.PORTRAIT,
                        onClick = { viewModel.onOrientationChanged(PdfOrientation.PORTRAIT) }
                    )
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_landscape),
                        selected = uiState.orientation == PdfOrientation.LANDSCAPE,
                        onClick = { viewModel.onOrientationChanged(PdfOrientation.LANDSCAPE) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

                OptionSection(label = stringResource(R.string.image_to_pdf_margins_label)) {
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_margin_none),
                        selected = uiState.margin == PdfMargin.NONE,
                        onClick = { viewModel.onMarginChanged(PdfMargin.NONE) }
                    )
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_margin_small),
                        selected = uiState.margin == PdfMargin.SMALL,
                        onClick = { viewModel.onMarginChanged(PdfMargin.SMALL) }
                    )
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_margin_medium),
                        selected = uiState.margin == PdfMargin.MEDIUM,
                        onClick = { viewModel.onMarginChanged(PdfMargin.MEDIUM) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

                OptionSection(label = stringResource(R.string.image_to_pdf_quality_label)) {
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_quality_original),
                        selected = uiState.quality == PdfOutputQuality.ORIGINAL,
                        onClick = { viewModel.onQualityChanged(PdfOutputQuality.ORIGINAL) }
                    )
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_quality_high),
                        selected = uiState.quality == PdfOutputQuality.HIGH,
                        onClick = { viewModel.onQualityChanged(PdfOutputQuality.HIGH) }
                    )
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_quality_medium),
                        selected = uiState.quality == PdfOutputQuality.MEDIUM,
                        onClick = { viewModel.onQualityChanged(PdfOutputQuality.MEDIUM) }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

                OptionSection(label = stringResource(R.string.image_to_pdf_fit_mode_label)) {
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_fit_to_page),
                        selected = uiState.fitMode == FitMode.FIT,
                        onClick = { viewModel.onFitModeChanged(FitMode.FIT) }
                    )
                    OptionRow(
                        text = stringResource(R.string.image_to_pdf_crop_to_fill),
                        selected = uiState.fitMode == FitMode.CROP,
                        onClick = { viewModel.onFitModeChanged(FitMode.CROP) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun ReorderableImageList(
    imageList: List<ImageEntry>,
    thumbnails: Map<String, ImageBitmap>,
    onRemove: (Int) -> Unit,
    onMove: (from: Int, to: Int) -> Unit
) {
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 64.dp.toPx() }
    val currentList by rememberUpdatedState(imageList)

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        imageList.forEachIndexed { index, entry ->
            key(entry.uri.toString()) {
                val itemKey = entry.uri.toString()
                ImageListItem(
                    entry = entry,
                    thumbnail = thumbnails[itemKey],
                    onRemove = { onRemove(index) },
                    dragHandleModifier = Modifier.pointerInput(itemKey) {
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
private fun ImageListItem(
    entry: ImageEntry,
    thumbnail: ImageBitmap?,
    onRemove: () -> Unit,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val metaLine = if (entry.isLoadingMetadata) {
        stringResource(R.string.image_to_pdf_loading_details)
    } else {
        buildString {
            if (entry.width > 0 && entry.height > 0) append("${entry.width}×${entry.height}")
            if (entry.width > 0 && entry.height > 0 && entry.fileSize > 0) append(" · ")
            if (entry.fileSize > 0) append(Formatter.formatShortFileSize(context, entry.fileSize))
        }
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

        Spacer(modifier = Modifier.size(Spacing.xs))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                Image(
                    bitmap = thumbnail,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(AppIconSize.small)
                )
            }
        }

        Spacer(modifier = Modifier.size(Spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.displayName,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (metaLine.isNotEmpty()) {
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

@Composable
private fun OptionSection(
    label: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Column(modifier = Modifier.selectableGroup()) {
            content()
        }
    }
}

@Composable
private fun OptionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = Spacing.sm)
        )
    }
}
