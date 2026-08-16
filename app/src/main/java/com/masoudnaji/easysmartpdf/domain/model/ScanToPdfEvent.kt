package com.masoudnaji.easysmartpdf.domain.model

sealed interface ScanToPdfEvent {
    data object Started : ScanToPdfEvent
    data class Progress(val current: Int, val total: Int) : ScanToPdfEvent
    data class Completed(val fileName: String) : ScanToPdfEvent
    data class Failed(val userMessage: String) : ScanToPdfEvent
}
