package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.data.ImageProcessor
import com.example.model.DensityConfig
import com.example.model.GeneratorSettings
import com.example.model.MipmapDensity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.zip.ZipFile

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mipmap Generator", appName)
    }

    @Test
    fun `verify all mipmap densities have standard Android sizes`() {
        assertEquals(48, MipmapDensity.MDPI.sizePx)
        assertEquals(72, MipmapDensity.HDPI.sizePx)
        assertEquals(96, MipmapDensity.XHDPI.sizePx)
        assertEquals(144, MipmapDensity.XXHDPI.sizePx)
        assertEquals(192, MipmapDensity.XXXHDPI.sizePx)
    }

    @Test
    fun `verify independent density rendering and zip export`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sampleBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)

        val settings = GeneratorSettings(resourceName = "ic_launcher")
        val renderedMap = mutableMapOf<MipmapDensity, Bitmap>()

        // Render each density with independent settings
        for (density in MipmapDensity.entries) {
            val config = DensityConfig(density = density, zoom = 1.2f, panX = 0.05f)
            val rendered = ImageProcessor.renderDensityBitmap(sampleBitmap, config, settings)
            assertEquals(density.sizePx, rendered.width)
            assertEquals(density.sizePx, rendered.height)
            renderedMap[density] = rendered
        }

        // Generate real ZIP archive
        val zip = ImageProcessor.generateZipArchive(context, renderedMap, settings)
        assertTrue(zip.exists())
        assertTrue(zip.length() > 0)

        // Verify ZIP internal folder structure
        ZipFile(zip).use { zf ->
            assertNotNull(zf.getEntry("res/"))
            for (density in MipmapDensity.entries) {
                assertNotNull(zf.getEntry("res/${density.folderName}/"))
                assertNotNull(zf.getEntry("res/${density.folderName}/ic_launcher.png"))
                assertNotNull(zf.getEntry("res/${density.folderName}/ic_launcher_round.png"))
            }
        }
    }
}
