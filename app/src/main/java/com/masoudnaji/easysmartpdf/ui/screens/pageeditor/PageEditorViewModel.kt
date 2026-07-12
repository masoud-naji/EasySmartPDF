package com.masoudnaji.easysmartpdf.ui.screens.pageeditor

import androidx.lifecycle.ViewModel
import com.masoudnaji.easysmartpdf.domain.model.PageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PageEditorViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PageEditorUiState())
    val uiState: StateFlow<PageEditorUiState> = _uiState.asStateFlow()

    fun loadPage(page: PageItem) {
        if (_uiState.value.page == null) {
            _uiState.update { it.copy(page = page) }
        }
    }

    fun rotateLeft() = applyRotationDelta(-90)
    fun rotateRight() = applyRotationDelta(90)

    private fun applyRotationDelta(delta: Int) {
        _uiState.update { state ->
            val page = state.page ?: return@update state
            state.copy(page = page.copy(rotation = (page.rotation + delta + 360) % 360))
        }
    }

    fun setFineRotation(degrees: Float) {
        _uiState.update { state ->
            val page = state.page ?: return@update state
            state.copy(page = page.copy(fineRotation = degrees.coerceIn(-45f, 45f)))
        }
    }

    fun getUpdatedPage(): PageItem? = _uiState.value.page
}
