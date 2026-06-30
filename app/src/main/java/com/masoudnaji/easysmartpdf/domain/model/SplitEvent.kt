package com.masoudnaji.easysmartpdf.domain.model

sealed class SplitEvent {
    object Started : SplitEvent()
    data class Progress(val current: Int, val total: Int) : SplitEvent()
    data class Completed(val fileCount: Int, val folderName: String) : SplitEvent()
    data class Failed(val userMessage: String) : SplitEvent()
}
