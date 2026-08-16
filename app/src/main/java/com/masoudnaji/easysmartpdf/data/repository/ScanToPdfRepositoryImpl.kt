package com.masoudnaji.easysmartpdf.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.domain.model.ScanToPdfEvent
import com.masoudnaji.easysmartpdf.domain.repository.ScanToPdfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ScanToPdfRepositoryImpl(private val context: Context) : ScanToPdfRepository {

    companion object {
        private const val TAG = "ScanToPdfRepo"
        private const val OUTPUT_RELATIVE_PATH = "Documents/Poonel"
        private const val MAX_DIM_PX = 3000
    }

    override fun createPdf(pages: List<PageItem>, outputFileName: String): Flow<ScanToPdfEvent> = flow {
        emit(ScanToPdfEvent.Started)
        val total = pages.size
        check(total > 0) { "No pages to process" }
        Log.d(TAG, "=== Scan to PDF started. $total images → $outputFileName ===")

        val document = PdfDocument()
        var outputPageNumber = 1
        var processed = 0

        try {
            for (page in pages) {
                val uri = page.sourceUri

                val exifOrientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                    ExifInterface(stream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL

                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, boundsOpts)
                }
                val srcW = boundsOpts.outWidth
                val srcH = boundsOpts.outHeight
                check(srcW > 0 && srcH > 0) { "Invalid image dimensions for $uri" }

                val sampleSize = calculateSampleSize(srcW, srcH)
                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                var bitmap = requireNotNull(
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, decodeOpts)
                    }
                ) { "Failed to decode image from $uri" }

                bitmap = applyExifRotation(bitmap, exifOrientation)

                val baseRotation = ((page.rotation % 360) + 360) % 360
                val totalRotation = baseRotation.toFloat() + page.fineRotation
                val swapDims = baseRotation == 90 || baseRotation == 270
                val imgW = bitmap.width
                val imgH = bitmap.height
                val outW = if (swapDims) imgH else imgW
                val outH = if (swapDims) imgW else imgH

                val pageInfo = PdfDocument.PageInfo.Builder(outW, outH, outputPageNumber++).create()
                val pdfPage = document.startPage(pageInfo)
                val canvas = pdfPage.canvas
                canvas.drawColor(Color.WHITE)

                if (totalRotation != 0f) {
                    val matrix = Matrix()
                    matrix.postRotate(totalRotation, imgW / 2f, imgH / 2f)
                    if (swapDims) {
                        matrix.postTranslate((outW - imgW) / 2f, (outH - imgH) / 2f)
                    }
                    canvas.drawBitmap(bitmap, matrix, null)
                } else {
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                }

                document.finishPage(pdfPage)
                bitmap.recycle()

                processed++
                Log.d(TAG, "  Page $processed/$total (rot=${page.rotation}°, fine=${page.fineRotation}°)")
                emit(ScanToPdfEvent.Progress(current = processed, total = total))
            }

            Log.d(TAG, "All pages written. Saving to MediaStore…")
            savePdf(document, outputFileName)
            Log.d(TAG, "=== Saved: $outputFileName ===")
            emit(ScanToPdfEvent.Completed(fileName = outputFileName))
        } finally {
            document.close()
            Log.d(TAG, "PdfDocument closed")
        }
    }.catch { e ->
        Log.e(TAG, "Scan to PDF failed", e)
        emit(ScanToPdfEvent.Failed(userMessage = "${e.javaClass.simpleName}: ${e.message}"))
    }.flowOn(Dispatchers.IO)

    private fun calculateSampleSize(srcW: Int, srcH: Int): Int {
        val maxSrc = maxOf(srcW, srcH)
        var sampleSize = 1
        while (maxSrc / sampleSize > MAX_DIM_PX) sampleSize *= 2
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

    private fun savePdf(document: PdfDocument, fileName: String): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, OUTPUT_RELATIVE_PATH)
                put(MediaStore.Files.FileColumns.IS_PENDING, 1)
            }
        }
        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else
            MediaStore.Files.getContentUri("external")

        val uri = requireNotNull(resolver.insert(collectionUri, values)) {
            "MediaStore insert returned null for $fileName"
        }
        resolver.openOutputStream(uri)?.use { document.writeTo(it) }
            ?: error("openOutputStream returned null for $uri")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.update(uri, ContentValues().apply {
                put(MediaStore.Files.FileColumns.IS_PENDING, 0)
            }, null, null)
        }
        return uri
    }
}
