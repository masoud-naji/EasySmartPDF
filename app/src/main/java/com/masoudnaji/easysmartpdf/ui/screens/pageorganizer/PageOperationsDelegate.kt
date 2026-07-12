package com.masoudnaji.easysmartpdf.ui.screens.pageorganizer

import com.masoudnaji.easysmartpdf.domain.model.PageItem
import com.masoudnaji.easysmartpdf.domain.model.PageOrganizerState
import com.masoudnaji.easysmartpdf.domain.usecase.LoadPageThumbnailsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PageOperationsDelegate(
    private val loadThumbnailsUseCase: LoadPageThumbnailsUseCase,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(PageOrganizerState())
    val state: StateFlow<PageOrganizerState> = _state.asStateFlow()

    private var thumbnailJob: Job? = null

    fun loadThumbnails(initialPages: List<PageItem>) {
        thumbnailJob?.cancel()
        _state.update { it.copy(pages = initialPages, thumbnailsLoaded = 0, thumbnailsTotal = initialPages.size) }
        thumbnailJob = scope.launch {
            loadThumbnailsUseCase(initialPages).collect { loaded ->
                _state.update { state ->
                    val idx = state.pages.indexOfFirst { it.id == loaded.id }
                    if (idx == -1) return@update state
                    val updated = state.pages.toMutableList()
                    val prev = updated[idx].thumbnail
                    if (prev != null && prev != loaded.thumbnail && !prev.isRecycled) prev.recycle()
                    updated[idx] = loaded
                    state.copy(pages = updated, thumbnailsLoaded = state.thumbnailsLoaded + 1)
                }
            }
        }
    }

    fun rotate(pageId: String, clockwise: Boolean) {
        _state.update { state ->
            val idx = state.pages.indexOfFirst { it.id == pageId }
            if (idx == -1) return@update state
            val updated = state.pages.toMutableList()
            val delta = if (clockwise) 90 else -90
            updated[idx] = updated[idx].copy(rotation = (updated[idx].rotation + delta + 360) % 360)
            state.copy(pages = updated)
        }
    }

    fun delete(pageId: String) {
        _state.update { state ->
            val idx = state.pages.indexOfFirst { it.id == pageId }
            if (idx == -1) return@update state
            val updated = state.pages.toMutableList()
            val removed = updated.removeAt(idx)
            removed.thumbnail?.let { if (!it.isRecycled) it.recycle() }
            state.copy(pages = updated)
        }
    }

    fun move(from: Int, to: Int) {
        val list = _state.value.pages.toMutableList()
        if (from < 0 || to < 0 || from >= list.size || to >= list.size) return
        val item = list.removeAt(from)
        list.add(to, item)
        _state.update { it.copy(pages = list) }
    }

    fun setZoomedPage(pageId: String?) {
        _state.update { it.copy(zoomedPageId = pageId) }
    }

    fun clear() {
        thumbnailJob?.cancel()
        thumbnailJob = null
        _state.value.pages.forEach { page ->
            page.thumbnail?.let { if (!it.isRecycled) it.recycle() }
        }
        _state.update { PageOrganizerState() }
    }
}