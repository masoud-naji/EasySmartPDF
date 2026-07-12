package com.masoudnaji.easysmartpdf.domain.model

sealed interface ThumbnailState {
    object Pending : ThumbnailState
    object Loading : ThumbnailState
    object Loaded  : ThumbnailState
    object Error   : ThumbnailState
}
