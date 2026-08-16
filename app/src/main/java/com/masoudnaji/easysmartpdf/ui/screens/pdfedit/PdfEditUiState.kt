package com.masoudnaji.easysmartpdf.ui.screens.pdfedit

import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.ui.screens.merge.PdfEntry

data class PdfEditUiState(
    val selectedFile: PdfEntry? = null,
    val pages: List<PageItem> = emptyList(),
    val thumbnailsLoaded: Int = 0,
    val thumbnailsTotal: Int = 0,
    val zoomedPageId: String? = null,
    val saveState: PdfEditSaveState = PdfEditSaveState.Idle,
    val errorMessage: String? = null
)

sealed interface PdfEditSaveState {
    object Idle : PdfEditSaveState
    object Started : PdfEditSaveState
    data class InProgress(val current: Int, val total: Int) : PdfEditSaveState
    object Cancelled : PdfEditSaveState
    data class Completed(val fileName: String) : PdfEditSaveState
}
