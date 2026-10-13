package com.example.model

enum class OutputFormat(val extension: String, val label: String) {
    PNG("png", "PNG (Recommended)"),
    JPG("jpg", "JPG (Not recommended)")
}

enum class BackgroundMode {
    TRANSPARENT,
    SOLID_COLOR
}

enum class IconShape(val label: String) {
    ROUNDED_SQUARE("Rounded"),
    CIRCLE("Circle"),
    SQUIRCLE("Squircle"),
    ORIGINAL("Original"),
    SQUARE("Square")
}

data class GeneratorSettings(
    val resourceName: String = "ic_launcher",
    val outputFormat: OutputFormat = OutputFormat.PNG,
    val backgroundMode: BackgroundMode = BackgroundMode.TRANSPARENT,
    val solidColor: Long = 0xFFFFFFFF, // ARGB
    val iconShape: IconShape = IconShape.ROUNDED_SQUARE
)
