package com.masoudnaji.easysmartpdf.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.masoudnaji.easysmartpdf.domain.model.ImageFormat
import com.masoudnaji.easysmartpdf.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageRepositoryImpl(private val context: Context) : ImageRepository {

    override suspend fun saveImage(
        bitmap: Bitmap,
        displayName: String,
        format: ImageFormat,
        quality: Int,
        relativeFolder: String
    ): Uri = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val fileName = "$displayName.${format.extension()}"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, format.mimeType())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, relativeFolder)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = requireNotNull(
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ) { "MediaStore insert returned null for $fileName" }

        resolver.openOutputStream(uri)?.use { stream ->
            bitmap.compress(format.compressFormat(), quality, stream)
        } ?: error("Cannot open output stream for $uri")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        uri
    }

    private fun ImageFormat.mimeType() = when (this) {
        ImageFormat.JPEG -> "image/jpeg"
        ImageFormat.PNG -> "image/png"
    }

    private fun ImageFormat.extension() = when (this) {
        ImageFormat.JPEG -> "jpg"
        ImageFormat.PNG -> "png"
    }

    private fun ImageFormat.compressFormat() = when (this) {
        ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
        ImageFormat.PNG -> Bitmap.CompressFormat.PNG
    }
}
