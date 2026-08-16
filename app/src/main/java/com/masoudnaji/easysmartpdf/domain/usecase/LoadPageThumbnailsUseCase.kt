package com.masoudnaji.easysmartpdf.domain.usecase

import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.domain.model.PageSourceType
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
            // Skip pages whose thumbnail is already loaded — avoids re-decoding on session append
            if (page.thumbnailState == ThumbnailState.Loaded && page.thumbnail != null) {
                emit(page)
                continue
            }
            val updated = try {
                val bitmap = when (page.sourceType) {
                    PageSourceType.PDF -> repository.renderThumbnail(
                        uri = page.sourceUri,
                        pageIndex = page.sourcePageIndex,
                        targetWidthPx = THUMBNAIL_WIDTH_PX
                    )
                    PageSourceType.IMAGE -> repository.renderImageThumbnail(
                        uri = page.sourceUri,
                        targetWidthPx = THUMBNAIL_WIDTH_PX
                    )
                }
                page.copy(thumbnail = bitmap, thumbnailState = ThumbnailState.Loaded)
            } catch (_: Exception) {
                page.copy(thumbnailState = ThumbnailState.Error)
            }
            emit(updated)
        }
    }
}
