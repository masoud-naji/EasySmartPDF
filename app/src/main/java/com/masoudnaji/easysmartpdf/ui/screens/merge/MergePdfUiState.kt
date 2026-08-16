package com.masoudnaji.easysmartpdf.ui.screens.merge

import android.net.Uri
import com.masoudnaji.easysmartpdf.domain.model.PageItem

data class PdfEntry(
    val uri: Uri,
    val displayName: String,
    val pageCount: Int = 0,
    val fileSize: Long = 0L,
    val isLoadingMetadata: Boolean = false
)

data class MergePdfUiState(
    // Phase 1 — file selection
    val pdfList: List<PdfEntry> = emptyList(),

    // Phase 2 — page editor
    val pages: List<PageItem> = emptyList(),
    val thumbnailsLoaded: Int = 0,
    val thumbnailsTotal: Int = 0,
    val zoomedPageId: String? = null,

    // Phase 3 — merge pipeline
    val mergeState: MergeState = MergeState.Idle,

    val errorMessage: String? = null
)

sealed interface MergeState {
    object Idle : MergeState
    object Started : MergeState
    data class InProgress(val current: Int, val total: Int) : MergeState
    object Cancelled : MergeState
    data class Completed(val fileName: String) : MergeState
    data class Failed(val message: String) : MergeState
}
