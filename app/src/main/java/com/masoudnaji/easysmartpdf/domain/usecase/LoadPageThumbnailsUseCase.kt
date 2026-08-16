package com.masoudnaji.easysmartpdf.domain.usecase

import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.domain.model.ThumbnailState
import com.masoudnaji.easysmartpdf.domain.repository.PageEditorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoadPageThumbnailsUseCase(private val repository: PageEditorRepository) {

    companion object {
        const val THUMBNAIL_WIDTH_PX = 400
    }

    operator fun invoke(pages: List<PageItem>): Flow<PageItem> = flow {
        for (page in pages) {
            val updated = try {
                val bitmap = repository.renderThumbnail(
                    uri = page.sourceUri,
                    pageIndex = page.sourcePageIndex,
                    targetWidthPx = THUMBNAIL_WIDTH_PX
                )
                page.copy(thumbnail = bitmap, thumbnailState = ThumbnailState.Loaded)
            } catch (_: Exception) {
                page.copy(thumbnailState = ThumbnailState.Error)
            }
            emit(updated)
        }
    }
}
