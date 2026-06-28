package com.masoudnaji.easysmartpdf.domain.model

import android.net.Uri

data class ConversionConfig(
    val pdfUri: Uri,
    val baseFileName: String,
    val folderName: String,
    val fromPage: Int,
    val toPage: Int,
    val imageFormat: ImageFormat,
    val imageQuality: ImageQuality
)
