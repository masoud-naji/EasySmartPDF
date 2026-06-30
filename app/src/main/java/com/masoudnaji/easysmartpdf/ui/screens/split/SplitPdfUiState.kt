package com.masoudnaji.easysmartpdf.ui.screens.split

import com.masoudnaji.easysmartpdf.domain.model.PdfInfo

data class SplitPdfUiState(
    val pdfInfo: PdfInfo? = null,
    val fileSize: Long = 0L,
    val pageMode: SplitPageMode = SplitPageMode.ALL,
    val fromPage: Int = 1,
    val toPage: Int = 1,
    val outputMode: SplitOutputMode = SplitOutputMode.SINGLE_PDF,
    val splitState: SplitState = SplitState.Idle,
    val errorMessage: String? = null
)

enum class SplitPageMode { ALL, RANGE }
enum class SplitOutputMode { SINGLE_PDF, SEPARATE_PDFS }

sealed interface SplitState {
    object Idle : SplitState
    object Started : SplitState
    data class InProgress(val current: Int, val total: Int) : SplitState
    object Cancelled : SplitState
    data class Completed(val fileCount: Int, val folderName: String) : SplitState
}
