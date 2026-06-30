package com.masoudnaji.easysmartpdf.domain.model

sealed class MergeEvent {
    object Started : MergeEvent()
    data class Progress(val current: Int, val total: Int) : MergeEvent()
    data class Completed(val fileName: String) : MergeEvent()
    data class Failed(val userMessage: String) : MergeEvent()
}
