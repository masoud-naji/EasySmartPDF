package com.masoudnaji.easysmartpdf.domain.model

import android.graphics.Bitmap

data class RenderedPage(
    val pageIndex: Int,
    val pageNumber: Int,
    val bitmap: Bitmap
)
