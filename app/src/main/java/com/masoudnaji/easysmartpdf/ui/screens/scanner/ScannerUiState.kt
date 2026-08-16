package com.masoudnaji.easysmartpdf.ui.screens.scanner

import com.masoudnaji.easysmartpdf.domain.model.PageItem

data class ScannerUiState(
    val pages: List<PageItem> = emptyList(),
    val thumbnailsLoaded: Int = 0,
    val thumbnailsTotal: Int = 0,
    val zoomedPageId: String? = null,
    val errorMessage: String? = null,
    val scanState: ScannerScanState = ScannerScanState.Idle
)

sealed interface ScannerScanState {
    data object Idle : ScannerScanState
    data object Started : ScannerScanState
    data class InProgress(val current: Int, val total: Int) : ScannerScanState
    data class Completed(val fileName: String) : ScannerScanState
    data object Cancelled : ScannerScanState
}
