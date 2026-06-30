package com.masoudnaji.easysmartpdf.domain.usecase

import com.masoudnaji.easysmartpdf.domain.model.SplitConfig
import com.masoudnaji.easysmartpdf.domain.model.SplitEvent
import com.masoudnaji.easysmartpdf.domain.repository.SplitRepository
import kotlinx.coroutines.flow.Flow

class SplitPdfUseCase(private val splitRepository: SplitRepository) {
    operator fun invoke(config: SplitConfig): Flow<SplitEvent> =
        splitRepository.splitPdf(config)
}
