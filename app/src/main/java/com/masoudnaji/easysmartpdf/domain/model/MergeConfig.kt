package com.masoudnaji.easysmartpdf.domain.model

data class MergeConfig(
    val pages: List<PageItem>,
    val outputFileName: String
)
