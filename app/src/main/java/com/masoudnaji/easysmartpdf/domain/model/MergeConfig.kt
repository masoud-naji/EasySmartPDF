package com.masoudnaji.easysmartpdf.domain.model

import android.net.Uri

data class MergeConfig(
    val pdfUris: List<Uri>,
    val outputFileName: String
)
