package com.masoudnaji.easysmartpdf.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.media.ExifInterface
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

    override suspend fun renderImageThumbnail(uri: Uri, targetWidthPx: Int): Bitmap =
        withContext(Dispatchers.IO) {
            // Pass 1: read dimensions without allocating pixel memory
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, boundsOpts)
            }
            val srcW = boundsOpts.outWidth
            val srcH = boundsOpts.outHeight
            check(srcW > 0 && srcH > 0) { "Cannot determine image dimensions for $uri" }

            // Pass 2: read EXIF orientation (cheap header-only read)
            val exifOrientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            // Pass 3: decode at reduced resolution
            val sampleSize = calculateInSampleSize(srcW, srcH, targetWidthPx)
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val raw = requireNotNull(
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, decodeOpts)
                }
            ) { "Failed to decode image from $uri" }

            applyExifRotation(raw, exifOrientation)
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

    private fun calculateInSampleSize(srcW: Int, srcH: Int, targetWidthPx: Int): Int {
        var sampleSize = 1
        if (srcW > targetWidthPx) {
            val halfW = srcW / 2
            while (halfW / sampleSize > targetWidthPx) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }

    private fun applyExifRotation(bitmap: Bitmap, orientation: Int): Bitmap {
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        return rotated
    }
}
