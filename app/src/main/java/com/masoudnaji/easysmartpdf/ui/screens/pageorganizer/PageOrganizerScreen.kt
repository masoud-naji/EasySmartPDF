package com.masoudnaji.easysmartpdf.ui.screens.pageorganizer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.ui.components.PageEditorGrid
import com.masoudnaji.easysmartpdf.ui.components.PrimaryButton
import com.masoudnaji.easysmartpdf.ui.components.ZoomPreviewDialog
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageOrganizerScreen(
    pages: List<PageItem>,
    thumbnailsLoaded: Int,
    thumbnailsTotal: Int,
    zoomedPageId: String?,
    errorMessage: String?,
    ctaText: String,
    onLoad: () -> Unit,
    onBackClick: () -> Unit,
    onRotateLeft: (pageId: String) -> Unit,
    onRotateRight: (pageId: String) -> Unit,
    onDelete: (pageId: String) -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    onPageClick: (pageId: String) -> Unit,
    onLongPressPage: (pageId: String) -> Unit,
    onZoomDismiss: () -> Unit,
    onErrorShown: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    bottomActions: (@Composable ColumnScope.() -> Unit)? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (pages.isEmpty()) onLoad()
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            onErrorShown()
        }
    }

    if (zoomedPageId != null && pages.isNotEmpty()) {
        ZoomPreviewDialog(
            pages = pages,
            initialPageId = zoomedPageId,
            onDismiss = onZoomDismiss
        )
    }

    val isLoading = thumbnailsTotal > 0 && thumbnailsLoaded < thumbnailsTotal

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.page_editor_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (isLoading) {
                            Text(
                                text = stringResource(
                                    R.string.page_editor_loading_progress,
                                    thumbnailsLoaded,
                                    thumbnailsTotal
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
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
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(Spacing.lg)
            ) {
                if (bottomActions != null) {
                    bottomActions()
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }
                PrimaryButton(
                    text = ctaText,
                    onClick = onConfirm,
                    enabled = pages.isNotEmpty()
                )
            }
        },
        modifier = modifier,
        containerColor = Color.Transparent
    ) { innerPadding ->
        PageEditorGrid(
            pages = pages,
            onRotateLeft = onRotateLeft,
            onRotateRight = onRotateRight,
            onDelete = onDelete,
            onPageClick = onPageClick,
            onLongClick = onLongPressPage,
            onMove = onMove,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.md,
                end = Spacing.md,
                top = innerPadding.calculateTopPadding() + Spacing.sm,
                bottom = innerPadding.calculateBottomPadding() + Spacing.sm
            )
        )
    }
}
