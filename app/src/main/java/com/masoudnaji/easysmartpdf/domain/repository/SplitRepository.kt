package com.masoudnaji.easysmartpdf.domain.repository

import com.masoudnaji.easysmartpdf.domain.model.SplitConfig
import com.masoudnaji.easysmartpdf.domain.model.SplitEvent
import kotlinx.coroutines.flow.Flow

interface SplitRepository {
    fun splitPdf(config: SplitConfig): Flow<SplitEvent>
}
