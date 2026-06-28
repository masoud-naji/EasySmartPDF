package com.masoudnaji.easysmartpdf.domain.repository

import android.net.Uri
import com.masoudnaji.easysmartpdf.domain.model.PdfInfo
import com.masoudnaji.easysmartpdf.domain.model.RenderedPage
import kotlinx.coroutines.flow.Flow

interface PdfRepository {
    suspend fun getPdfInfo(uri: Uri): PdfInfo
    fun renderPages(uri: Uri, pageIndices: List<Int>, scaleFactor: Float): Flow<RenderedPage>
}
