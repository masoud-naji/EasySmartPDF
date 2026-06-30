package com.masoudnaji.easysmartpdf.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.masoudnaji.easysmartpdf.domain.model.FitMode
import com.masoudnaji.easysmartpdf.domain.model.PdfOutputQuality
import com.masoudnaji.easysmartpdf.domain.model.ImageToPdfConfig
import com.masoudnaji.easysmartpdf.domain.model.ImageToPdfEvent
import com.masoudnaji.easysmartpdf.domain.model.PdfMargin
import com.masoudnaji.easysmartpdf.domain.model.PdfOrientation
import com.masoudnaji.easysmartpdf.domain.model.PdfPageSize
import com.masoudnaji.easysmartpdf.domain.repository.ImageToPdfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class ImageToPdfRepositoryImpl(private val context: Context) : ImageToPdfRepository {

    override fun createPdf(config: ImageToPdfConfig): Flow<ImageToPdfEvent> = flow {
        emit(ImageToPdfEvent.Started)

        val (pageWidth, pageHeight) = pageDimensions(config.pageSize, config.orientation, config.quality)
        val marginPx = marginPixels(config.margin, pageWidth, pageHeight)
        val document = PdfDocument()

        try {
            val total = config.imageUris.size
            config.imageUris.forEachIndexed { idx, uri ->
                val bitmap = loadBitmap(uri, pageWidth, pageHeight)
                    ?: error("Could not load image ${idx + 1}")

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, idx + 1).create()
                val pdfPage = document.startPage(pageInfo)
                val canvas = pdfPage.canvas

                val bgPaint = Paint().apply { color = Color.WHITE }
                canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
                drawImage(canvas, bitmap, pageWidth, pageHeight, marginPx, config.fitMode)
                bitmap.recycle()

                document.finishPage(pdfPage)
                emit(ImageToPdfEvent.Progress(current = idx + 1, total = total))
            }

            saveDocument(document, config.outputFileName)
            emit(ImageToPdfEvent.Completed(fileName = config.outputFileName))
        } finally {
            document.close()
        }
    }.catch { e ->
        Log.e(TAG, "Image to PDF failed", e)
        emit(ImageToPdfEvent.Failed("We couldn't create your PDF. Please try again."))
    }.flowOn(Dispatchers.IO)

    private fun pageDimensions(
        pageSize: PdfPageSize,
        orientation: PdfOrientation,
        quality: PdfOutputQuality
    ): Pair<Int, Int> {
        val (portW, portH) = when (quality) {
            PdfOutputQuality.ORIGINAL -> when (pageSize) {
                PdfPageSize.A4 -> 2480 to 3508
                PdfPageSize.LETTER -> 2550 to 3300
            }
            PdfOutputQuality.HIGH -> when (pageSize) {
                PdfPageSize.A4 -> 1240 to 1754
                PdfPageSize.LETTER -> 1275 to 1650
            }
            PdfOutputQuality.MEDIUM -> when (pageSize) {
                PdfPageSize.A4 -> 595 to 842
                PdfPageSize.LETTER -> 612 to 792
            }
        }
        return if (orientation == PdfOrientation.PORTRAIT) portW to portH else portH to portW
    }

    private fun marginPixels(margin: PdfMargin, pageWidth: Int, pageHeight: Int): Int {
        val shorter = minOf(pageWidth, pageHeight)
        return when (margin) {
            PdfMargin.NONE -> 0
            PdfMargin.SMALL -> (shorter * 0.035f).toInt()
            PdfMargin.MEDIUM -> (shorter * 0.075f).toInt()
        }
    }

    private fun loadBitmap(uri: Uri, pageWidth: Int, pageHeight: Int): Bitmap? {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, boundsOpts)
        }
        val srcW = boundsOpts.outWidth
        val srcH = boundsOpts.outHeight
        if (srcW <= 0 || srcH <= 0) return null

        // Target 2× page dimensions so downscaling to page size stays sharp
        val sampleSize = calculateInSampleSize(srcW, srcH, pageWidth * 2, pageHeight * 2)
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        }
    }

    private fun drawImage(
        canvas: android.graphics.Canvas,
        bitmap: Bitmap,
        pageWidth: Int,
        pageHeight: Int,
        marginPx: Int,
        fitMode: FitMode
    ) {
        val availW = (pageWidth - 2 * marginPx).toFloat()
        val availH = (pageHeight - 2 * marginPx).toFloat()
        val bitmapW = bitmap.width.toFloat()
        val bitmapH = bitmap.height.toFloat()

        val scale = when (fitMode) {
            FitMode.FIT -> minOf(availW / bitmapW, availH / bitmapH)
            FitMode.CROP -> maxOf(availW / bitmapW, availH / bitmapH)
        }

        val scaledW = bitmapW * scale
        val scaledH = bitmapH * scale
        val left = marginPx + (availW - scaledW) / 2f
        val top = marginPx + (availH - scaledH) / 2f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        if (fitMode == FitMode.CROP) {
            canvas.save()
            canvas.clipRect(
                marginPx.toFloat(), marginPx.toFloat(),
                (pageWidth - marginPx).toFloat(), (pageHeight - marginPx).toFloat()
            )
        }
        canvas.drawBitmap(bitmap, null, RectF(left, top, left + scaledW, top + scaledH), paint)
        if (fitMode == FitMode.CROP) canvas.restore()
    }

    private fun saveDocument(document: PdfDocument, fileName: String) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, "Documents/EasySmartPDF")
                put(MediaStore.Files.FileColumns.IS_PENDING, 1)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else
            MediaStore.Files.getContentUri("external")

        val uri = requireNotNull(resolver.insert(collection, values)) {
            "MediaStore insert returned null for $fileName"
        }
        resolver.openOutputStream(uri)?.use { document.writeTo(it) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.update(uri, ContentValues().apply {
                put(MediaStore.Files.FileColumns.IS_PENDING, 0)
            }, null, null)
        }
        Log.d(TAG, "Saved PDF: $uri")
    }

    private fun calculateInSampleSize(srcW: Int, srcH: Int, reqW: Int, reqH: Int): Int {
        var sampleSize = 1
        if (srcH > reqH || srcW > reqW) {
            val halfH = srcH / 2
            val halfW = srcW / 2
            while ((halfH / sampleSize) > reqH || (halfW / sampleSize) > reqW) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }

    companion object {
        private const val TAG = "ImageToPdfRepository"
    }
}
