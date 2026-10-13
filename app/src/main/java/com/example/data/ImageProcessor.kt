package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.BackgroundMode
import com.example.model.DensityConfig
import com.example.model.GeneratorSettings
import com.example.model.IconShape
import com.example.model.MipmapDensity
import com.example.model.OutputFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ImageProcessor {

    suspend fun createDefaultSampleBitmap(context: Context): Bitmap = withContext(Dispatchers.Default) {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_android_robot)
        if (drawable != null) {
            val padding = 48
            drawable.setBounds(padding, padding, size - padding, size - padding)
            drawable.draw(canvas)
        } else {
            val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#3DDC84")
            }
            val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
            }
            canvas.drawArc(RectF(156f, 130f, 356f, 330f), 180f, 180f, true, greenPaint)
            canvas.drawCircle(206f, 195f, 10f, whitePaint)
            canvas.drawCircle(306f, 195f, 10f, whitePaint)
            canvas.drawRoundRect(RectF(156f, 240f, 356f, 390f), 20f, 20f, greenPaint)
            canvas.drawRoundRect(RectF(110f, 240f, 142f, 360f), 16f, 16f, greenPaint)
            canvas.drawRoundRect(RectF(370f, 240f, 402f, 360f), 16f, 16f, greenPaint)
            canvas.drawRoundRect(RectF(190f, 390f, 230f, 450f), 16f, 16f, greenPaint)
            canvas.drawRoundRect(RectF(282f, 390f, 322f, 450f), 16f, 16f, greenPaint)
        }
        bitmap
    }

    suspend fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val maxDimension = 1024
            var sampleSize = 1
            while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            inputStream = context.contentResolver.openInputStream(uri)
            val result = android.graphics.BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun renderDensityBitmap(
        source: Bitmap,
        config: DensityConfig,
        settings: GeneratorSettings,
        overrideShape: IconShape? = null
    ): Bitmap {
        val targetSize = config.density.sizePx
        val result = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val shape = overrideShape ?: settings.iconShape
        val s = targetSize.toFloat()

        if (shape != IconShape.ORIGINAL) {
            val clipPath = Path().apply {
                when (shape) {
                    IconShape.CIRCLE -> {
                        addCircle(s / 2f, s / 2f, s / 2f, Path.Direction.CW)
                    }
                    IconShape.ROUNDED_SQUARE -> {
                        val radius = s * 0.22f
                        addRoundRect(RectF(0f, 0f, s, s), radius, radius, Path.Direction.CW)
                    }
                    IconShape.SQUIRCLE -> {
                        val radius = s * 0.35f
                        addRoundRect(RectF(0f, 0f, s, s), radius, radius, Path.Direction.CW)
                    }
                    IconShape.SQUARE -> {
                        addRect(RectF(0f, 0f, s, s), Path.Direction.CW)
                    }
                    IconShape.ORIGINAL -> {}
                }
            }
            canvas.clipPath(clipPath)
        }

        // Draw background inside clipped shape
        if (settings.backgroundMode == BackgroundMode.SOLID_COLOR) {
            canvas.drawColor(settings.solidColor.toInt())
        } else if (settings.outputFormat == OutputFormat.JPG) {
            canvas.drawColor(Color.WHITE)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Compute aspect-fit dimensions
        val srcW = source.width.toFloat()
        val srcH = source.height.toFloat()
        val scale = minOf(targetSize / srcW, targetSize / srcH) * config.zoom

        val scaledW = srcW * scale
        val scaledH = srcH * scale

        val left = (targetSize - scaledW) / 2f + config.panX * targetSize
        val top = (targetSize - scaledH) / 2f + config.panY * targetSize

        val destRect = RectF(left, top, left + scaledW, top + scaledH)
        val srcRect = Rect(0, 0, source.width, source.height)

        canvas.drawBitmap(source, srcRect, destRect, paint)
        return result
    }

    suspend fun generateZipArchive(
        context: Context,
        densityBitmaps: Map<MipmapDensity, Bitmap>,
        settings: GeneratorSettings,
        sourceBitmap: Bitmap? = null,
        densityConfigs: Map<MipmapDensity, DensityConfig> = emptyMap()
    ): File = withContext(Dispatchers.IO) {
        val safeResName = settings.resourceName.trim().ifEmpty { "ic_launcher" }
            .replace(Regex("[^a-zA-Z0-9_]"), "_")
        val ext = settings.outputFormat.extension
        val compressFormat = if (settings.outputFormat == OutputFormat.PNG) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }

        val zipFile = File(context.cacheDir, "mipmap-resources.zip")
        if (zipFile.exists()) {
            zipFile.delete()
        }

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // Root "res/" directory entry
            zos.putNextEntry(ZipEntry("res/"))
            zos.closeEntry()

            for (density in MipmapDensity.entries) {
                val folder = "res/${density.folderName}/"
                zos.putNextEntry(ZipEntry(folder))
                zos.closeEntry()

                // 1. Standard launcher icon in chosen shape
                val standardBitmap = densityBitmaps[density]
                if (standardBitmap != null) {
                    val entryPath = "$folder$safeResName.$ext"
                    zos.putNextEntry(ZipEntry(entryPath))
                    val stream = ByteArrayOutputStream()
                    standardBitmap.compress(compressFormat, 100, stream)
                    zos.write(stream.toByteArray())
                    zos.closeEntry()
                }

                // 2. Round launcher icon (ic_launcher_round) in Circle shape
                val config = densityConfigs[density] ?: DensityConfig(density)
                val roundBitmap = if (sourceBitmap != null) {
                    renderDensityBitmap(sourceBitmap, config, settings, overrideShape = IconShape.CIRCLE)
                } else {
                    standardBitmap
                }

                if (roundBitmap != null) {
                    val roundEntryPath = "$folder${safeResName}_round.$ext"
                    zos.putNextEntry(ZipEntry(roundEntryPath))
                    val roundStream = ByteArrayOutputStream()
                    roundBitmap.compress(compressFormat, 100, roundStream)
                    zos.write(roundStream.toByteArray())
                    zos.closeEntry()
                }
            }
        }

        zipFile
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024f)
            else -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
        }
    }
}
