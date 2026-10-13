package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ImageProcessor
import com.example.model.BackgroundMode
import com.example.model.DensityConfig
import com.example.model.GeneratorSettings
import com.example.model.MipmapDensity
import com.example.model.OutputFormat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
    SPLASH,                  // 1
    HOME,                    // 2
    IMAGE_UPLOAD,            // 3
    PROCESSING,              // 4
    ALL_PREVIEWS,            // 5
    DENSITY_EDITOR,          // 6-10
    SETTINGS,                // 11
    CONFIRM_GENERATE,        // 12
    GENERATING_ZIP,          // 13
    DOWNLOAD_COMPLETE,       // 14
    ZIP_STRUCTURE_PREVIEW    // 15
}

data class MipmapUiState(
    val currentScreen: AppScreen = AppScreen.SPLASH,
    val sourceBitmap: Bitmap? = null,
    val imageFileName: String = "android_logo.png",
    val imageWidth: Int = 512,
    val imageHeight: Int = 512,
    val imageFileSize: Long = 124 * 1024L,
    val densityConfigs: Map<MipmapDensity, DensityConfig> = MipmapDensity.entries.associateWith {
        DensityConfig(density = it)
    },
    val selectedDensityForEdit: MipmapDensity = MipmapDensity.MDPI,
    val settings: GeneratorSettings = GeneratorSettings(),
    val renderedBitmaps: Map<MipmapDensity, Bitmap> = emptyMap(),
    val generatedZipFile: File? = null,
    val zipFileSizeText: String = "2.4 MB • 5 folders",
    val isProcessing: Boolean = false,
    val processingProgress: Float = 0.75f,
    val isGeneratingZip: Boolean = false,
    val zipProgress: Float = 0.80f
)

class MipmapViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MipmapUiState())
    val uiState: StateFlow<MipmapUiState> = _uiState.asStateFlow()

    fun initDefaultImage(context: Context) {
        if (_uiState.value.sourceBitmap == null) {
            viewModelScope.launch {
                val defaultBmp = ImageProcessor.createDefaultSampleBitmap(context)
                _uiState.update {
                    it.copy(
                        sourceBitmap = defaultBmp,
                        imageFileName = "android_logo.png",
                        imageWidth = defaultBmp.width,
                        imageHeight = defaultBmp.height,
                        imageFileSize = 124 * 1024L
                    )
                }
                regenerateRenderedBitmaps()
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun openDensityEditor(density: MipmapDensity) {
        _uiState.update {
            it.copy(
                selectedDensityForEdit = density,
                currentScreen = AppScreen.DENSITY_EDITOR
            )
        }
    }

    fun onImagePicked(context: Context, uri: Uri) {
        viewModelScope.launch {
            val bitmap = ImageProcessor.loadBitmapFromUri(context, uri)
            if (bitmap != null) {
                val fileName = getFileName(context, uri) ?: "uploaded_image.png"
                val fileSize = getFileSize(context, uri)
                _uiState.update {
                    it.copy(
                        sourceBitmap = bitmap,
                        imageFileName = fileName,
                        imageWidth = bitmap.width,
                        imageHeight = bitmap.height,
                        imageFileSize = fileSize
                    )
                }
                regenerateRenderedBitmaps()
                startProcessingWorkflow()
            }
        }
    }

    fun onBitmapLoaded(bitmap: Bitmap, fileName: String = "camera_photo.png") {
        viewModelScope.launch {
            val approximateSize = (bitmap.width * bitmap.height * 4L).coerceIn(50 * 1024L, 2 * 1024 * 1024L)
            _uiState.update {
                it.copy(
                    sourceBitmap = bitmap,
                    imageFileName = fileName,
                    imageWidth = bitmap.width,
                    imageHeight = bitmap.height,
                    imageFileSize = approximateSize
                )
            }
            regenerateRenderedBitmaps()
            startProcessingWorkflow()
        }
    }

    fun startProcessingWorkflow() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.PROCESSING,
                    isProcessing = true,
                    processingProgress = 0.25f
                )
            }
            delay(400)
            _uiState.update { it.copy(processingProgress = 0.50f) }
            delay(400)
            _uiState.update { it.copy(processingProgress = 0.75f) }
            regenerateRenderedBitmaps()
            delay(400)
            _uiState.update {
                it.copy(
                    processingProgress = 1.0f,
                    isProcessing = false,
                    currentScreen = AppScreen.ALL_PREVIEWS
                )
            }
        }
    }

    fun updateDensityZoom(density: MipmapDensity, zoom: Float) {
        val current = _uiState.value.densityConfigs[density] ?: DensityConfig(density)
        val updated = current.copy(zoom = zoom.coerceIn(0.2f, 3.0f))
        val newConfigs = _uiState.value.densityConfigs.toMutableMap()
        newConfigs[density] = updated
        _uiState.update { it.copy(densityConfigs = newConfigs) }
        regenerateRenderedBitmaps()
    }

    fun updateDensityPan(density: MipmapDensity, panX: Float, panY: Float) {
        val current = _uiState.value.densityConfigs[density] ?: DensityConfig(density)
        val updated = current.copy(panX = panX, panY = panY)
        val newConfigs = _uiState.value.densityConfigs.toMutableMap()
        newConfigs[density] = updated
        _uiState.update { it.copy(densityConfigs = newConfigs) }
        regenerateRenderedBitmaps()
    }

    fun saveDensityEditor(density: MipmapDensity) {
        val current = _uiState.value.densityConfigs[density] ?: DensityConfig(density)
        val updated = current.copy(isEdited = true)
        val newConfigs = _uiState.value.densityConfigs.toMutableMap()
        newConfigs[density] = updated
        _uiState.update {
            it.copy(
                densityConfigs = newConfigs,
                currentScreen = AppScreen.ALL_PREVIEWS
            )
        }
        regenerateRenderedBitmaps()
    }

    fun resetDensityConfig(density: MipmapDensity) {
        val resetConfig = DensityConfig(density = density)
        val newConfigs = _uiState.value.densityConfigs.toMutableMap()
        newConfigs[density] = resetConfig
        _uiState.update { it.copy(densityConfigs = newConfigs) }
        regenerateRenderedBitmaps()
    }

    fun updateSettings(
        resourceName: String,
        outputFormat: OutputFormat,
        backgroundMode: BackgroundMode,
        solidColor: Long,
        iconShape: com.example.model.IconShape
    ) {
        _uiState.update {
            it.copy(
                settings = GeneratorSettings(
                    resourceName = resourceName.trim().ifEmpty { "ic_launcher" },
                    outputFormat = outputFormat,
                    backgroundMode = backgroundMode,
                    solidColor = solidColor,
                    iconShape = iconShape
                )
            )
        }
        regenerateRenderedBitmaps()
    }

    fun updateIconShape(shape: com.example.model.IconShape) {
        _uiState.update {
            it.copy(settings = it.settings.copy(iconShape = shape))
        }
        regenerateRenderedBitmaps()
    }

    fun generateZip(context: Context) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.GENERATING_ZIP,
                    isGeneratingZip = true,
                    zipProgress = 0.25f
                )
            }
            delay(400)
            _uiState.update { it.copy(zipProgress = 0.50f) }
            delay(400)
            _uiState.update { it.copy(zipProgress = 0.80f) }

            val zip = ImageProcessor.generateZipArchive(
                context = context,
                densityBitmaps = _uiState.value.renderedBitmaps,
                settings = _uiState.value.settings,
                sourceBitmap = _uiState.value.sourceBitmap,
                densityConfigs = _uiState.value.densityConfigs
            )

            val formattedSize = ImageProcessor.formatFileSize(zip.length()) + " • 5 folders"
            delay(400)
            _uiState.update {
                it.copy(
                    zipProgress = 1.0f,
                    isGeneratingZip = false,
                    generatedZipFile = zip,
                    zipFileSizeText = formattedSize,
                    currentScreen = AppScreen.DOWNLOAD_COMPLETE
                )
            }
        }
    }

    private fun regenerateRenderedBitmaps() {
        val source = _uiState.value.sourceBitmap ?: return
        val currentSettings = _uiState.value.settings
        val currentConfigs = _uiState.value.densityConfigs

        val resultMap = mutableMapOf<MipmapDensity, Bitmap>()
        for (density in MipmapDensity.entries) {
            val config = currentConfigs[density] ?: DensityConfig(density)
            resultMap[density] = ImageProcessor.renderDensityBitmap(source, config, currentSettings)
        }
        _uiState.update { it.copy(renderedBitmaps = resultMap) }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        var size: Long = 0
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (index != -1) {
                    size = it.getLong(index)
                }
            }
        }
        return if (size > 0) size else 124 * 1024L
    }
}
