package com.masoudnaji.easysmartpdf.domain.model

import android.net.Uri

enum class PdfPageSize { A4, LETTER }
enum class PdfOrientation { PORTRAIT, LANDSCAPE }
enum class PdfMargin { NONE, SMALL, MEDIUM }
enum class PdfOutputQuality { ORIGINAL, HIGH, MEDIUM }
enum class FitMode { FIT, CROP }

data class ImageToPdfConfig(
    val imageUris: List<Uri>,
    val outputFileName: String,
    val pageSize: PdfPageSize,
    val orientation: PdfOrientation,
    val margin: PdfMargin,
    val quality: PdfOutputQuality,
    val fitMode: FitMode
)
