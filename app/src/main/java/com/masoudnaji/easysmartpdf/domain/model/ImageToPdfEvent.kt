package com.masoudnaji.easysmartpdf.domain.model

sealed class ImageToPdfEvent {
    object Started : ImageToPdfEvent()
    data class Progress(val current: Int, val total: Int) : ImageToPdfEvent()
    data class Completed(val fileName: String) : ImageToPdfEvent()
    data class Failed(val userMessage: String) : ImageToPdfEvent()
}
