package com.masoudnaji.easysmartpdf.domain.usecase

import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.domain.model.ScanToPdfEvent
import com.masoudnaji.easysmartpdf.domain.repository.ScanToPdfRepository
import kotlinx.coroutines.flow.Flow

class ScanToPdfUseCase(private val repository: ScanToPdfRepository) {
    operator fun invoke(pages: List<PageItem>, outputFileName: String): Flow<ScanToPdfEvent> =
        repository.createPdf(pages, outputFileName)
}
