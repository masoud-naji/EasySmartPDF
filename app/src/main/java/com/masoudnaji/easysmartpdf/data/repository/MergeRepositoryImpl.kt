package com.masoudnaji.easysmartpdf.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.masoudnaji.easysmartpdf.domain.model.MergeConfig
import com.masoudnaji.easysmartpdf.domain.model.MergeEvent
import com.masoudnaji.easysmartpdf.domain.repository.MergeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MergeRepositoryImpl(private val context: Context) : MergeRepository {

    companion object {
        private const val TAG = "MergeRepo"
        // MediaStore.Files only allows Download or Documents as the root directory
        private const val OUTPUT_RELATIVE_PATH = "Documents/EasySmartPDF"
    }

    override fun mergePdfs(config: MergeConfig): Flow<MergeEvent> = flow {
        emit(MergeEvent.Started)
        Log.d(TAG, "=== Merge started. ${config.pdfUris.size} files → ${config.outputFileName} ===")

        // Step 1: Count total pages (each call gets its own file descriptor)
        val pageCounts = config.pdfUris.mapIndexed { idx, uri ->
            Log.d(TAG, "Counting pages [$idx]: $uri")
            // Do NOT use pfd.use here — PdfRenderer takes ownership of the pfd and closes it
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd == null) {
                Log.e(TAG, "Cannot open file descriptor [$idx]")
                return@mapIndexed 0
            }
            val count = PdfRenderer(pfd).use { it.pageCount }
            Log.d(TAG, "  → $count pages")
            count
        }
        val totalPages = pageCounts.sum()
        Log.d(TAG, "Total pages to merge: $totalPages")
        check(totalPages > 0) { "No readable pages found across all source PDFs" }

        val document = PdfDocument()
        var pageNumber = 1
        var processed = 0

        try {
            config.pdfUris.forEachIndexed { fileIndex, uri ->
                Log.d(TAG, "Opening source PDF [$fileIndex]: $uri")
                // PdfRenderer takes ownership of the pfd and closes it when closed —
                // do NOT also wrap pfd in .use to avoid double-close
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                    ?: error("Cannot open file descriptor for PDF [$fileIndex]")

                PdfRenderer(pfd).use { renderer ->
                    val count = renderer.pageCount
                    Log.d(TAG, "  Renderer opened. Pages: $count")
                    for (i in 0 until count) {
                        val page = renderer.openPage(i)
                        val w = page.width
                        val h = page.height
                        Log.d(TAG, "  Copying page $i ($w×$h) → output page $pageNumber")

                        check(w > 0 && h > 0) { "Page $i of PDF [$fileIndex] has invalid dimensions ${w}×${h}" }

                        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        page.close()

                        val pageInfo = PdfDocument.PageInfo.Builder(w, h, pageNumber++).create()
                        val pdfPage = document.startPage(pageInfo)
                        pdfPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                        document.finishPage(pdfPage)
                        bitmap.recycle()

                        processed++
                        emit(MergeEvent.Progress(current = processed, total = totalPages))
                    }
                }
                Log.d(TAG, "PDF [$fileIndex] fully copied")
            }

            Log.d(TAG, "All pages copied. Saving to MediaStore...")
            val savedUri = saveMergedPdf(document, config.outputFileName)
            Log.d(TAG, "=== Saved merged PDF: $savedUri ===")

            emit(MergeEvent.Completed(fileName = config.outputFileName))
        } finally {
            document.close()
            Log.d(TAG, "PdfDocument closed")
        }
    }.catch { e ->
        Log.e(TAG, "Merge pipeline failed", e)
        emit(MergeEvent.Failed(userMessage = "${e.javaClass.simpleName}: ${e.message}"))
    }.flowOn(Dispatchers.IO)

    private fun saveMergedPdf(document: PdfDocument, fileName: String): Uri {
        val resolver = context.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, OUTPUT_RELATIVE_PATH)
                put(MediaStore.Files.FileColumns.IS_PENDING, 1)
            }
        }
        Log.d(TAG, "Inserting into MediaStore. RELATIVE_PATH=$OUTPUT_RELATIVE_PATH, API=${Build.VERSION.SDK_INT}")

        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        Log.d(TAG, "Collection URI: $collectionUri")

        val uri = requireNotNull(resolver.insert(collectionUri, values)) {
            "MediaStore insert returned null for $fileName"
        }
        Log.d(TAG, "MediaStore row created: $uri")

        resolver.openOutputStream(uri)?.use { stream ->
            Log.d(TAG, "Writing document to stream...")
            document.writeTo(stream)
            Log.d(TAG, "writeTo() complete")
        } ?: error("openOutputStream returned null for $uri")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pending = ContentValues().apply {
                put(MediaStore.Files.FileColumns.IS_PENDING, 0)
            }
            val rows = resolver.update(uri, pending, null, null)
            Log.d(TAG, "IS_PENDING cleared. Updated rows: $rows")
        }

        Log.d(TAG, "=== Final output URI: $uri ===")
        return uri
    }
}
