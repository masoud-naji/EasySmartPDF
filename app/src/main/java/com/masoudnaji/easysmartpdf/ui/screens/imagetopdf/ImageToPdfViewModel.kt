package com.masoudnaji.easysmartpdf.ui.screens.imagetopdf

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.masoudnaji.easysmartpdf.data.repository.ImageToPdfRepositoryImpl
import com.masoudnaji.easysmartpdf.domain.model.FitMode
import com.masoudnaji.easysmartpdf.domain.model.ImageEntry
import com.masoudnaji.easysmartpdf.domain.model.PdfOutputQuality
import com.masoudnaji.easysmartpdf.domain.model.ImageToPdfConfig
import com.masoudnaji.easysmartpdf.domain.model.ImageToPdfEvent
import com.masoudnaji.easysmartpdf.domain.model.PdfMargin
import com.masoudnaji.easysmartpdf.domain.model.PdfOrientation
import com.masoudnaji.easysmartpdf.domain.model.PdfPageSize
import com.masoudnaji.easysmartpdf.domain.usecase.ImageToPdfUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageToPdfViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val imageToPdfUseCase = ImageToPdfUseCase(ImageToPdfRepositoryImpl(context))

    private val _uiState = MutableStateFlow(ImageToPdfUiState())
    val uiState: StateFlow<ImageToPdfUiState> = _uiState.asStateFlow()

    private val _thumbnails = MutableStateFlow<Map<String, ImageBitmap>>(emptyMap())
    val thumbnails: StateFlow<Map<String, ImageBitmap>> = _thumbnails.asStateFlow()

    private var createJob: Job? = null

    fun addImage(uri: Uri) {
        if (_uiState.value.imageList.any { it.uri == uri }) {
            _uiState.update { it.copy(errorMessage = "This image has already been added.") }
            return
        }
        // Phase 1: add immediately so the filename shows at once (fast ContentResolver DB query)
        val displayName = queryDisplayName(uri)
        _uiState.update { state ->
            state.copy(imageList = state.imageList + ImageEntry(uri, displayName, isLoadingMetadata = true))
        }
        // Phase 2: load dimensions, file size, and thumbnail on IO thread
        viewModelScope.launch {
            val (width, height, fileSize) = withContext(Dispatchers.IO) { readImageMetadata(uri) }
            val thumbnail = withContext(Dispatchers.IO) { loadThumbnail(uri) }

            // Guard: entry may have been removed while IO was running
            if (_uiState.value.imageList.none { it.uri == uri }) return@launch

            _uiState.update { state ->
                state.copy(
                    imageList = state.imageList.map { entry ->
                        if (entry.uri == uri) entry.copy(
                            width = width,
                            height = height,
                            fileSize = fileSize,
                            isLoadingMetadata = false
                        )
                        else entry
                    }
                )
            }
            if (thumbnail != null) {
                _thumbnails.update { it + (uri.toString() to thumbnail) }
            }
            checkAutoOrientation()
        }
    }

    fun removeImage(index: Int) {
        val list = _uiState.value.imageList
        if (index !in list.indices) return
        val uri = list[index].uri
        _uiState.update { it.copy(imageList = list.toMutableList().also { it.removeAt(index) }) }
        _thumbnails.update { it - uri.toString() }
        checkAutoOrientation()
    }

    fun moveImage(from: Int, to: Int) {
        val list = _uiState.value.imageList.toMutableList()
        if (from < 0 || to < 0 || from >= list.size || to >= list.size) return
        val item = list.removeAt(from)
        list.add(to, item)
        _uiState.update { it.copy(imageList = list) }
    }

    fun onPageSizeChanged(size: PdfPageSize) = _uiState.update { it.copy(pageSize = size) }

    fun onOrientationChanged(orientation: PdfOrientation) {
        _uiState.update { it.copy(orientation = orientation, orientationIsManual = true) }
    }

    fun onMarginChanged(margin: PdfMargin) = _uiState.update { it.copy(margin = margin) }

    fun onQualityChanged(quality: PdfOutputQuality) = _uiState.update { it.copy(quality = quality) }

    fun onFitModeChanged(fitMode: FitMode) = _uiState.update { it.copy(fitMode = fitMode) }

    fun onErrorDismissed() = _uiState.update { it.copy(errorMessage = null) }

    fun cancelCreation() {
        createJob?.cancel()
        _uiState.update { it.copy(createState = ImageToPdfState.Cancelled) }
    }

    fun startCreation() {
        val state = _uiState.value
        val cs = state.createState
        if (cs !is ImageToPdfState.Idle && cs !is ImageToPdfState.Cancelled) return
        if (state.imageList.isEmpty()) return

        val config = ImageToPdfConfig(
            imageUris = state.imageList.map { it.uri },
            outputFileName = buildOutputFileName(),
            pageSize = state.pageSize,
            orientation = state.orientation,
            margin = state.margin,
            quality = state.quality,
            fitMode = state.fitMode
        )

        createJob = viewModelScope.launch {
            imageToPdfUseCase(config).collect { event ->
                when (event) {
                    is ImageToPdfEvent.Started ->
                        _uiState.update { it.copy(createState = ImageToPdfState.Started) }
                    is ImageToPdfEvent.Progress ->
                        _uiState.update { it.copy(createState = ImageToPdfState.InProgress(event.current, event.total)) }
                    is ImageToPdfEvent.Completed ->
                        _uiState.update { it.copy(createState = ImageToPdfState.Completed(event.fileName)) }
                    is ImageToPdfEvent.Failed ->
                        _uiState.update { it.copy(createState = ImageToPdfState.Idle, errorMessage = event.userMessage) }
                }
            }
        }
    }

    private fun checkAutoOrientation() {
        val state = _uiState.value
        if (state.orientationIsManual || state.imageList.size != 1) return
        val entry = state.imageList.firstOrNull() ?: return
        if (entry.width <= 0 || entry.height <= 0) return
        val auto = if (entry.width >= entry.height) PdfOrientation.LANDSCAPE else PdfOrientation.PORTRAIT
        if (state.orientation != auto) {
            _uiState.update { it.copy(orientation = auto) }
        }
    }

    private fun buildOutputFileName(): String {
        val ts = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
        return "Images_$ts.pdf"
    }

    private fun queryDisplayName(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        ?: uri.lastPathSegment ?: "image"
                }
            }
        return uri.lastPathSegment ?: "image"
    }

    private fun readImageMetadata(uri: Uri): Triple<Int, Int, Long> {
        val fileSize = context.contentResolver
            .query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst())
                    cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
                else 0L
            } ?: 0L

        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        return Triple(opts.outWidth.coerceAtLeast(0), opts.outHeight.coerceAtLeast(0), fileSize)
    }

    private fun loadThumbnail(uri: Uri): ImageBitmap? {
        return try {
            val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(uri, android.util.Size(128, 128), null)
            } else {
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, boundsOpts)
                }
                val sampleSize = calculateInSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, 128, 128)
                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, decodeOpts)
                }
            }
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            Log.w(TAG, "Thumbnail load failed for $uri", e)
            null
        }
    }

    private fun calculateInSampleSize(srcW: Int, srcH: Int, reqW: Int, reqH: Int): Int {
        var sampleSize = 1
        if (srcH > reqH || srcW > reqW) {
            val halfH = srcH / 2
            val halfW = srcW / 2
            while ((halfH / sampleSize) > reqH || (halfW / sampleSize) > reqW) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }

    companion object {
        private const val TAG = "ImageToPdfViewModel"
    }
}
