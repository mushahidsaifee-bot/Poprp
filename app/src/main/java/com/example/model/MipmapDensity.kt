package com.example.model

enum class MipmapDensity(
    val idName: String,
    val sizePx: Int,
    val folderName: String
) {
    MDPI("MDPI", 48, "mipmap-mdpi"),
    HDPI("HDPI", 72, "mipmap-hdpi"),
    XHDPI("XHDPI", 96, "mipmap-xhdpi"),
    XXHDPI("XXHDPI", 144, "mipmap-xxhdpi"),
    XXXHDPI("XXXHDPI", 192, "mipmap-xxxhdpi");

    val dimensionLabel: String
        get() = "$sizePx x $sizePx px"

    val sizeBadge: String
        get() = "$sizePx x $sizePx px"

    companion object {
        fun fromId(id: String): MipmapDensity {
            return entries.firstOrNull { it.name.equals(id, ignoreCase = true) } ?: MDPI
        }
    }
}
