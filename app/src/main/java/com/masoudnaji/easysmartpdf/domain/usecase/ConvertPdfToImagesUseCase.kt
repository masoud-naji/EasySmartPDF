package com.masoudnaji.easysmartpdf.domain.usecase

import com.masoudnaji.easysmartpdf.domain.model.ConversionConfig
import com.masoudnaji.easysmartpdf.domain.model.ConversionEvent
import com.masoudnaji.easysmartpdf.domain.repository.ImageRepository
import com.masoudnaji.easysmartpdf.domain.repository.PdfRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ConvertPdfToImagesUseCase(
    private val pdfRepository: PdfRepository,
    private val imageRepository: ImageRepository
) {
    operator fun invoke(config: ConversionConfig): Flow<ConversionEvent> = flow {
        emit(ConversionEvent.Started)

        val pageIndices = (config.fromPage - 1 until config.toPage).toList()
        val total = pageIndices.size
        val savedUris = mutableListOf<android.net.Uri>()

        val relativeFolder = "Pictures/EasySmartPDF/${config.folderName}"
        pdfRepository.renderPages(
            uri = config.pdfUri,
            pageIndices = pageIndices,
            scaleFactor = config.imageQuality.scaleFactor
        ).collect { renderedPage ->
            emit(ConversionEvent.Progress(current = savedUris.size + 1, total = total))

            val displayName = "page_%03d".format(renderedPage.pageNumber)
            val savedUri = imageRepository.saveImage(
                bitmap = renderedPage.bitmap,
                displayName = displayName,
                format = config.imageFormat,
                quality = config.imageQuality.jpegQuality,
                relativeFolder = relativeFolder
            )
            renderedPage.bitmap.recycle()
            savedUris.add(savedUri)

            emit(
                ConversionEvent.PageSaved(
                    pageNumber = renderedPage.pageNumber,
                    total = total,
                    savedUri = savedUri
                )
            )
        }

        emit(ConversionEvent.Completed(savedCount = savedUris.size, savedUris = savedUris))
    }.catch { e ->
        emit(ConversionEvent.Failed(userMessage = "We couldn't create your pictures. Please try another PDF."))
    }
}
