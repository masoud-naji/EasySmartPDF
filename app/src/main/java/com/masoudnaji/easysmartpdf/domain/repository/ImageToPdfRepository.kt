package com.masoudnaji.easysmartpdf.domain.repository

import com.masoudnaji.easysmartpdf.domain.model.ImageToPdfConfig
import com.masoudnaji.easysmartpdf.domain.model.ImageToPdfEvent
import kotlinx.coroutines.flow.Flow

interface ImageToPdfRepository {
    fun createPdf(config: ImageToPdfConfig): Flow<ImageToPdfEvent>
}
