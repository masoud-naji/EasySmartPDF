package com.masoudnaji.easysmartpdf.ui.screens.merge

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.ui.components.PageEditorGrid
import com.masoudnaji.easysmartpdf.ui.components.PrimaryButton
import com.masoudnaji.easysmartpdf.ui.components.ZoomPreviewDialog
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergePageEditorScreen(
    mergePdfEntry: NavBackStackEntry,
    onBackClick: () -> Unit,
    onNavigateToProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: MergePdfViewModel = viewModel(mergePdfEntry)
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (uiState.pages.isEmpty()) {
            viewModel.loadPageThumbnails()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            snackbarHostState.showSnackbar(uiState.errorMessage!!)
            viewModel.onErrorDismissed()
        }
    }

    val zoomedPageId = uiState.zoomedPageId
    if (zoomedPageId != null && uiState.pages.isNotEmpty()) {
        ZoomPreviewDialog(
            pages = uiState.pages,
            initialPageId = zoomedPageId,
            onDismiss = { viewModel.setZoomedPage(null) }
        )
    }

    val pageCount = uiState.pages.size
    val loaded = uiState.thumbnailsLoaded
    val total = uiState.thumbnailsTotal
    val isLoading = total > 0 && loaded < total

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
                                text = stringResource(R.string.page_editor_loading_progress, loaded, total),
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
                PrimaryButton(
                    text = if (pageCount > 0)
                        pluralStringResource(R.plurals.page_editor_merge_action, pageCount, pageCount)
                    else
                        stringResource(R.string.merge_action),
                    onClick = {
                        viewModel.startMerge()
                        onNavigateToProgress()
                    },
                    enabled = pageCount >= 1
                )
            }
        },
        modifier = modifier,
        containerColor = Color.Transparent
    ) { innerPadding ->
        PageEditorGrid(
            pages = uiState.pages,
            onRotateLeft = { pageId -> viewModel.rotatePage(pageId, clockwise = false) },
            onRotateRight = { pageId -> viewModel.rotatePage(pageId, clockwise = true) },
            onDelete = { pageId -> viewModel.deletePage(pageId) },
            onPageClick = { pageId -> viewModel.setZoomedPage(pageId) },
            onMove = { from, to -> viewModel.movePage(from, to) },
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
