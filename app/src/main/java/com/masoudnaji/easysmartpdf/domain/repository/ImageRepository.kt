package com.masoudnaji.easysmartpdf.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import com.masoudnaji.easysmartpdf.domain.model.ImageFormat

interface ImageRepository {
    suspend fun saveImage(
        bitmap: Bitmap,
        displayName: String,
        format: ImageFormat,
        quality: Int,
        relativeFolder: String
    ): Uri
}
