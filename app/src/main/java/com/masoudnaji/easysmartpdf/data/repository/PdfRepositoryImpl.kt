package com.masoudnaji.easysmartpdf.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import com.masoudnaji.easysmartpdf.domain.model.PdfInfo
import com.masoudnaji.easysmartpdf.domain.model.RenderedPage
import com.masoudnaji.easysmartpdf.domain.repository.PdfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class PdfRepositoryImpl(private val context: Context) : PdfRepository {

    override suspend fun getPdfInfo(uri: Uri): PdfInfo = withContext(Dispatchers.IO) {
        val fileName = queryFileName(uri)
        val pageCount = countPages(uri)
        PdfInfo(fileName = fileName, pageCount = pageCount)
    }

    override fun renderPages(
        uri: Uri,
        pageIndices: List<Int>,
        scaleFactor: Float
    ): Flow<RenderedPage> = flow {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            ?: error("Cannot open file descriptor for PDF")

        pfd.use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                for (pageIndex in pageIndices) {
                    check(pageIndex in 0 until renderer.pageCount) {
                        "Page $pageIndex is out of range (total: ${renderer.pageCount})"
                    }
                    renderer.openPage(pageIndex).use { page ->
                        val width = (page.width * scaleFactor).toInt().coerceAtLeast(1)
                        val height = (page.height * scaleFactor).toInt().coerceAtLeast(1)
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        emit(RenderedPage(pageIndex = pageIndex, pageNumber = pageIndex + 1, bitmap = bitmap))
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun queryFileName(uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (col != -1) return cursor.getString(col) ?: ""
            }
        }
        return uri.lastPathSegment ?: "document.pdf"
    }

    private fun countPages(uri: Uri): Int {
        return context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
            PdfRenderer(pfd).use { renderer -> renderer.pageCount }
        } ?: 0
    }
}
