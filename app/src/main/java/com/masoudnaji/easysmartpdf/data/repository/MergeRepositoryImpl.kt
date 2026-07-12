package com.masoudnaji.easysmartpdf.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.masoudnaji.easysmartpdf.domain.model.MergeConfig
import com.masoudnaji.easysmartpdf.domain.model.MergeEvent
import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.domain.repository.MergeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MergeRepositoryImpl(private val context: Context) : MergeRepository {

    companion object {
        private const val TAG = "MergeRepo"
        private const val OUTPUT_RELATIVE_PATH = "Documents/Poonel"
    }

    override fun mergePdfs(config: MergeConfig): Flow<MergeEvent> = flow {
        emit(MergeEvent.Started)
        val total = config.pages.size
        Log.d(TAG, "=== Merge started. $total pages → ${config.outputFileName} ===")
        check(total > 0) { "No pages to merge" }

        val document = PdfDocument()
        var outputPageNumber = 1
        var processed = 0

        try {
            // Group consecutive pages from the same URI to minimise open/close cycles.
            val groups = config.pages.groupByUri()

            for ((uri, pagesInFile) in groups) {
                Log.d(TAG, "Opening $uri (${pagesInFile.size} pages to copy)")
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                    ?: error("Cannot open file descriptor for $uri")

                PdfRenderer(pfd).use { renderer ->
                    for (pageItem in pagesInFile) {
                        val srcIdx = pageItem.sourcePageIndex
                        check(srcIdx in 0 until renderer.pageCount) {
                            "Page index $srcIdx out of range in $uri"
                        }

                        renderer.openPage(srcIdx).use { page ->
                            val srcW = page.width
                            val srcH = page.height
                            check(srcW > 0 && srcH > 0) {
                                "Page $srcIdx in $uri has invalid dimensions ${srcW}×${srcH}"
                            }

                            val baseRotation = pageItem.rotation.normalizeRotation()
                            val fineRotation = pageItem.fineRotation
                            val totalRotation = baseRotation.toFloat() + fineRotation
                            val swapDims = baseRotation == 90 || baseRotation == 270
                            val outW = if (swapDims) srcH else srcW
                            val outH = if (swapDims) srcW else srcH

                            val bitmap = Bitmap.createBitmap(srcW, srcH, Bitmap.Config.ARGB_8888)
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)

                            val pageInfo = PdfDocument.PageInfo.Builder(outW, outH, outputPageNumber++).create()
                            val pdfPage = document.startPage(pageInfo)
                            val canvas = pdfPage.canvas

                            if (totalRotation != 0f) {
                                val matrix = Matrix()
                                matrix.postRotate(totalRotation, srcW / 2f, srcH / 2f)
                                if (swapDims) {
                                    matrix.postTranslate(
                                        (outW - srcW) / 2f,
                                        (outH - srcH) / 2f
                                    )
                                }
                                canvas.drawBitmap(bitmap, matrix, null)
                            } else {
                                canvas.drawBitmap(bitmap, 0f, 0f, null)
                            }

                            document.finishPage(pdfPage)
                            bitmap.recycle()
                        }

                        processed++
                        Log.d(TAG, "  Wrote page $processed/$total (src=$srcIdx, rot=${pageItem.rotation}°, fine=${pageItem.fineRotation}°)")
                        emit(MergeEvent.Progress(current = processed, total = total))
                    }
                }
                Log.d(TAG, "Closed $uri")
            }

            Log.d(TAG, "All pages written. Saving to MediaStore…")
            val savedUri = saveMergedPdf(document, config.outputFileName)
            Log.d(TAG, "=== Saved: $savedUri ===")
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

    // Preserve the original order of pages while grouping consecutive pages by URI,
    // so we open each PdfRenderer file once per contiguous run rather than once per page.
    private fun List<PageItem>.groupByUri(): List<Pair<android.net.Uri, List<PageItem>>> {
        if (isEmpty()) return emptyList()
        val result = mutableListOf<Pair<android.net.Uri, MutableList<PageItem>>>()
        for (page in this) {
            if (result.isEmpty() || result.last().first != page.sourceUri) {
                result.add(Pair(page.sourceUri, mutableListOf(page)))
            } else {
                result.last().second.add(page)
            }
        }
        return result
    }

    private fun Int.normalizeRotation(): Int = ((this % 360) + 360) % 360
}
