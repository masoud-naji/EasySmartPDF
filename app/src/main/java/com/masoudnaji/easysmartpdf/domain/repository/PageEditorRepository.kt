package com.masoudnaji.easysmartpdf.domain.repository

import android.graphics.Bitmap
import android.net.Uri

interface PageEditorRepository {
    suspend fun renderThumbnail(uri: Uri, pageIndex: Int, targetWidthPx: Int): Bitmap
    suspend fun getPageCount(uri: Uri): Int
}
