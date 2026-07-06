package com.masoudnaji.easysmartpdf.domain.usecase

import com.masoudnaji.easysmartpdf.domain.model.ImageToPdfConfig
import com.masoudnaji.easysmartpdf.domain.model.ImageToPdfEvent
import com.masoudnaji.easysmartpdf.domain.repository.ImageToPdfRepository
import kotlinx.coroutines.flow.Flow

class ImageToPdfUseCase(private val repository: ImageToPdfRepository) {
    operator fun invoke(config: ImageToPdfConfig): Flow<ImageToPdfEvent> =
        repository.createPdf(config)
}
