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
import com.masoudnaji.easysmartpdf.domain.model.SplitConfig
import com.masoudnaji.easysmartpdf.domain.model.SplitEvent
import com.masoudnaji.easysmartpdf.domain.repository.SplitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SplitRepositoryImpl(private val context: Context) : SplitRepository {

    companion object {
        private const val TAG = "SplitRepo"
    }

    override fun splitPdf(config: SplitConfig): Flow<SplitEvent> = flow {
        emit(SplitEvent.Started)
        Log.d(TAG, "=== Split started. separatePdfs=${config.separatePdfs} pages=${config.pagesToSplit} folder=${config.folderName} ===")

        val pfd = context.contentResolver.openFileDescriptor(config.pdfUri, "r")
            ?: error("Cannot open file descriptor for source PDF")

        if (config.separatePdfs) {
            // One PDF per page saved into a subfolder
            PdfRenderer(pfd).use { renderer ->
                val total = config.pagesToSplit.size
                config.pagesToSplit.forEachIndexed { idx, pageNum ->
                    val (w, h, bitmap) = renderPage(renderer, pageNum)
                    val singleDoc = PdfDocument()
                    try {
                        val pageInfo = PdfDocument.PageInfo.Builder(w, h, 1).create()
                        val pdfPage = singleDoc.startPage(pageInfo)
                        pdfPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                        singleDoc.finishPage(pdfPage)

                        val fileName = "page_%03d.pdf".format(pageNum)
                        saveDocument(singleDoc, fileName, subFolder = config.folderName)
                        Log.d(TAG, "Saved $fileName")
                    } finally {
                        singleDoc.close()
                        bitmap.recycle()
                    }
                    emit(SplitEvent.Progress(current = idx + 1, total = total))
                }
            }
            Log.d(TAG, "=== Separate PDFs complete. ${config.pagesToSplit.size} files ===")
            emit(SplitEvent.Completed(fileCount = config.pagesToSplit.size, folderName = config.folderName))
        } else {
            // All selected pages merged into one PDF
            val multiDoc = PdfDocument()
            try {
                PdfRenderer(pfd).use { renderer ->
                    val total = config.pagesToSplit.size
                    config.pagesToSplit.forEachIndexed { idx, pageNum ->
                        val (w, h, bitmap) = renderPage(renderer, pageNum)
                        val pageInfo = PdfDocument.PageInfo.Builder(w, h, idx + 1).create()
                        val pdfPage = multiDoc.startPage(pageInfo)
                        pdfPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                        multiDoc.finishPage(pdfPage)
                        bitmap.recycle()
                        emit(SplitEvent.Progress(current = idx + 1, total = total))
                    }
                }
                val fileName = "${config.folderName}.pdf"
                saveDocument(multiDoc, fileName, subFolder = "")
                Log.d(TAG, "=== Single PDF complete: $fileName ===")
            } finally {
                multiDoc.close()
            }
            emit(SplitEvent.Completed(fileCount = 1, folderName = ""))
        }
    }.catch { e ->
        Log.e(TAG, "Split pipeline failed", e)
        emit(SplitEvent.Failed(userMessage = "We couldn't split this PDF. Please try again."))
    }.flowOn(Dispatchers.IO)

    private data class RenderedPageData(val width: Int, val height: Int, val bitmap: Bitmap)

    private fun renderPage(renderer: PdfRenderer, pageNum: Int): RenderedPageData {
        val pageIndex = pageNum - 1
        check(pageIndex in 0 until renderer.pageCount) {
            "Page $pageNum is out of range (PDF has ${renderer.pageCount} pages)"
        }
        val page = renderer.openPage(pageIndex)
        val w = page.width
        val h = page.height
        check(w > 0 && h > 0) { "Page $pageNum has invalid dimensions ${w}×${h}" }

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
        page.close()
        return RenderedPageData(w, h, bitmap)
    }

    private fun saveDocument(document: PdfDocument, fileName: String, subFolder: String): Uri {
        val relativePath = if (subFolder.isNotEmpty())
            "Documents/EasySmartPDF/Split/$subFolder"
        else
            "Documents/EasySmartPDF/Split"
        val resolver = context.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Files.FileColumns.RELATIVE_PATH, relativePath)
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

        resolver.openOutputStream(uri)?.use { stream ->
            document.writeTo(stream)
        } ?: error("openOutputStream returned null for $uri")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pending = ContentValues().apply {
                put(MediaStore.Files.FileColumns.IS_PENDING, 0)
            }
            resolver.update(uri, pending, null, null)
        }

        return uri
    }
}
