package com.masoudnaji.easysmartpdf.domain.model

import android.net.Uri

data class ImageEntry(
    val uri: Uri,
    val displayName: String,
    val width: Int = 0,
    val height: Int = 0,
    val fileSize: Long = 0L,
    val isLoadingMetadata: Boolean = false
)
