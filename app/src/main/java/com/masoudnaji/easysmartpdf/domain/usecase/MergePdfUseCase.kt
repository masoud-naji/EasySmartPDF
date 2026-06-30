package com.masoudnaji.easysmartpdf.domain.usecase

import com.masoudnaji.easysmartpdf.domain.model.MergeConfig
import com.masoudnaji.easysmartpdf.domain.model.MergeEvent
import com.masoudnaji.easysmartpdf.domain.repository.MergeRepository
import kotlinx.coroutines.flow.Flow

class MergePdfUseCase(private val repository: MergeRepository) {
    operator fun invoke(config: MergeConfig): Flow<MergeEvent> = repository.mergePdfs(config)
}
