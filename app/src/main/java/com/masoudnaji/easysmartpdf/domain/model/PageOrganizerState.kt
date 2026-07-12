package com.masoudnaji.easysmartpdf.domain.model

data class PageOrganizerState(
    val pages: List<PageItem> = emptyList(),
    val thumbnailsLoaded: Int = 0,
    val thumbnailsTotal: Int = 0,
    val zoomedPageId: String? = null
)