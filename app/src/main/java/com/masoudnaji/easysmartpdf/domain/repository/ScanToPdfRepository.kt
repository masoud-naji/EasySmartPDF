package com.masoudnaji.easysmartpdf.domain.repository

import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.domain.model.ScanToPdfEvent
import kotlinx.coroutines.flow.Flow

interface ScanToPdfRepository {
    fun createPdf(pages: List<PageItem>, outputFileName: String): Flow<ScanToPdfEvent>
}
