package com.masoudnaji.easysmartpdf.ui.screens.merge

import android.net.Uri

data class PdfEntry(
    val uri: Uri,
    val displayName: String,
    val pageCount: Int = 0,
    val fileSize: Long = 0L,
    val isLoadingMetadata: Boolean = false
)

data class MergePdfUiState(
    val pdfList: List<PdfEntry> = emptyList(),
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
