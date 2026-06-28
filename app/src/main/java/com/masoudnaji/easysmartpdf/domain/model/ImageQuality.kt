package com.masoudnaji.easysmartpdf.domain.model

enum class ImageQuality(val scaleFactor: Float, val jpegQuality: Int) {
    SMALLER(scaleFactor = 1.5f, jpegQuality = 72),
    BALANCED(scaleFactor = 2.0f, jpegQuality = 85),
    BEST(scaleFactor = 3.0f, jpegQuality = 95)
}
