package com.masoudnaji.easysmartpdf.domain.model

import android.net.Uri

data class SplitConfig(
    val pdfUri: Uri,
    val folderName: String,
    val pagesToSplit: List<Int>,
    val separatePdfs: Boolean
)
