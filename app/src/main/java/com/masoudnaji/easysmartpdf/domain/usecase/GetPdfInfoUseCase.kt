package com.masoudnaji.easysmartpdf.domain.usecase

import android.net.Uri
import com.masoudnaji.easysmartpdf.domain.model.PdfInfo
import com.masoudnaji.easysmartpdf.domain.repository.PdfRepository

class GetPdfInfoUseCase(private val pdfRepository: PdfRepository) {
    suspend operator fun invoke(uri: Uri): PdfInfo = pdfRepository.getPdfInfo(uri)
}
