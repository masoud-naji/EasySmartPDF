package com.masoudnaji.easysmartpdf.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.masoudnaji.easysmartpdf.domain.repository.PageEditorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class PageEditorRepositoryImpl(private val context: Context) : PageEditorRepository {

    override suspend fun renderThumbnail(uri: Uri, pageIndex: Int, targetWidthPx: Int): Bitmap =
        withContext(Dispatchers.IO) {
            val pfd = requireNotNull(context.contentResolver.openFileDescriptor(uri, "r")) {
                "Cannot open file descriptor for $uri"
            }
            PdfRenderer(pfd).use { renderer ->
                check(pageIndex in 0 until renderer.pageCount) {
                    "Page index $pageIndex out of range (${renderer.pageCount} pages)"
                }
                renderer.openPage(pageIndex).use { page ->
                    val scale = targetWidthPx.toFloat() / page.width.toFloat()
                    val w = targetWidthPx
                    val h = (page.height * scale).roundToInt().coerceAtLeast(1)

                    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap
                }
            }
        }

    override suspend fun getPageCount(uri: Uri): Int =
        withContext(Dispatchers.IO) {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext 0
            try {
                PdfRenderer(pfd).use { it.pageCount }
            } catch (_: Exception) {
                0
            }
        }
}
