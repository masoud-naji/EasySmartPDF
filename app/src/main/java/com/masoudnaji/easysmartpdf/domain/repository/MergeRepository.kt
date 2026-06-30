package com.masoudnaji.easysmartpdf.domain.repository

import com.masoudnaji.easysmartpdf.domain.model.MergeConfig
import com.masoudnaji.easysmartpdf.domain.model.MergeEvent
import kotlinx.coroutines.flow.Flow

interface MergeRepository {
    fun mergePdfs(config: MergeConfig): Flow<MergeEvent>
}
