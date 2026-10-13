package com.example.model

data class DensityConfig(
    val density: MipmapDensity,
    val zoom: Float = 1.0f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val isEdited: Boolean = false
) {
    fun reset(): DensityConfig = copy(
        zoom = 1.0f,
        panX = 0f,
        panY = 0f,
        isEdited = false
    )
}
