package com.masoudnaji.easysmartpdf.domain.model

import android.net.Uri

sealed class ConversionEvent {
    object Started : ConversionEvent()
    data class Progress(val current: Int, val total: Int) : ConversionEvent()
    data class PageSaved(val pageNumber: Int, val total: Int, val savedUri: Uri) : ConversionEvent()
    data class Completed(val savedCount: Int, val savedUris: List<Uri>) : ConversionEvent()
    data class Failed(val userMessage: String) : ConversionEvent()
}
