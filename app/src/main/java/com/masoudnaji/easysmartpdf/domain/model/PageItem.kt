package com.masoudnaji.easysmartpdf.domain.model

import android.graphics.Bitmap
import android.net.Uri

data class PageItem(
    val id: String,
    val sourceUri: Uri,
    val sourcePageIndex: Int,
    val sourceFileName: String,
    val rotation: Int = 0,
    val thumbnail: Bitmap? = null,
    val thumbnailState: ThumbnailState = ThumbnailState.Pending
)
