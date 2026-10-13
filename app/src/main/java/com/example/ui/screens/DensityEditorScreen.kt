package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DensityConfig
import com.example.model.MipmapDensity
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.SuccessGreen

enum class KeylineOverlayMode(val label: String) {
    SAFE_ZONE("Safe Zone"),
    ROUNDED("Rounded"),
    CIRCLE("Circle"),
    SQUIRCLE("Squircle"),
    NONE("Off")
}

@Composable
fun DensityEditorScreen(
    density: MipmapDensity,
    sourceBitmap: Bitmap?,
    config: DensityConfig,
    onBack: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onPanChange: (Float, Float) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localZoom by remember(config.zoom) { mutableFloatStateOf(config.zoom) }
    var localPanX by remember(config.panX) { mutableFloatStateOf(config.panX) }
    var localPanY by remember(config.panY) { mutableFloatStateOf(config.panY) }
    var activeOverlay by remember { mutableStateOf(KeylineOverlayMode.SAFE_ZONE) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Slate50
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("editor_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Slate800,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "${density.idName} Editor",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Slate800
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryBlueLight)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = density.sizeBadge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Guide toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (mode in KeylineOverlayMode.entries) {
                        val isSelected = activeOverlay == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryBlue else Color.White)
                                .border(
                                    BorderStroke(1.dp, if (isSelected) PrimaryBlue else Slate200),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { activeOverlay = mode }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Slate700,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Canvas Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.1f)
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("editor_canvas_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(density) {
                                detectTransformGestures { _, pan, gestureZoom, _ ->
                                    val newZoom = (localZoom * gestureZoom).coerceIn(0.2f, 3.0f)
                                    localZoom = newZoom
                                    onZoomChange(newZoom)

                                    val newPanX = localPanX + (pan.x / size.width)
                                    val newPanY = localPanY + (pan.y / size.height)
                                    localPanX = newPanX
                                    localPanY = newPanY
                                    onPanChange(newPanX, newPanY)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val centerX = w / 2f
                            val centerY = h / 2f

                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                            drawLine(
                                color = Color(0xFFCBD5E1),
                                start = Offset(0f, centerY),
                                end = Offset(w, centerY),
                                strokeWidth = 1.5f,
                                pathEffect = dashEffect
                            )
                            drawLine(
                                color = Color(0xFFCBD5E1),
                                start = Offset(centerX, 0f),
                                end = Offset(centerX, h),
                                strokeWidth = 1.5f,
                                pathEffect = dashEffect
                            )

                            if (sourceBitmap != null) {
                                val baseSize = minOf(w, h) * 0.45f
                                val scaledWidth = baseSize * localZoom
                                val scaledHeight = baseSize * localZoom

                                val left = centerX - (scaledWidth / 2f) + (localPanX * w)
                                val top = centerY - (scaledHeight / 2f) + (localPanY * h)

                                drawImage(
                                    image = sourceBitmap.asImageBitmap(),
                                    dstOffset = IntOffset(left.toInt(), top.toInt()),
                                    dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt())
                                )

                                val boxPadding = 6f
                                val boxRect = Size(scaledWidth + boxPadding * 2, scaledHeight + boxPadding * 2)
                                val boxOffset = Offset(left - boxPadding, top - boxPadding)

                                drawRect(
                                    color = PrimaryBlue,
                                    topLeft = boxOffset,
                                    size = boxRect,
                                    style = Stroke(
                                        width = 2f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                                    )
                                )

                                val handleRadius = 6f
                                val handlePoints = listOf(
                                    boxOffset,
                                    Offset(boxOffset.x + boxRect.width / 2f, boxOffset.y),
                                    Offset(boxOffset.x + boxRect.width, boxOffset.y),
                                    Offset(boxOffset.x + boxRect.width, boxOffset.y + boxRect.height / 2f),
                                    Offset(boxOffset.x + boxRect.width, boxOffset.y + boxRect.height),
                                    Offset(boxOffset.x + boxRect.width / 2f, boxOffset.y + boxRect.height),
                                    Offset(boxOffset.x, boxOffset.y + boxRect.height),
                                    Offset(boxOffset.x, boxOffset.y + boxRect.height / 2f)
                                )

                                for (point in handlePoints) {
                                    drawCircle(
                                        color = PrimaryBlue,
                                        radius = handleRadius,
                                        center = point
                                    )
                                }
                            }

                            val safeRadius = (minOf(w, h) * 0.66f) / 2f

                            when (activeOverlay) {
                                KeylineOverlayMode.SAFE_ZONE -> {
                                    drawCircle(
                                        color = Color(0xFF10B981),
                                        radius = safeRadius,
                                        center = Offset(centerX, centerY),
                                        style = Stroke(
                                            width = 2f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                        )
                                    )
                                }
                                KeylineOverlayMode.ROUNDED -> {
                                    val cornerRadius = safeRadius * 2f * 0.22f
                                    val rect = Rect(centerX - safeRadius, centerY - safeRadius, centerX + safeRadius, centerY + safeRadius)
                                    val maskPath = Path().apply {
                                        fillType = PathFillType.EvenOdd
                                        addRect(Rect(0f, 0f, w, h))
                                        addRoundRect(androidx.compose.ui.geometry.RoundRect(rect, androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)))
                                    }
                                    drawPath(maskPath, color = Color(0x330F172A))
                                    drawRoundRect(
                                        color = PrimaryBlue,
                                        topLeft = Offset(rect.left, rect.top),
                                        size = Size(rect.width, rect.height),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                                        style = Stroke(width = 2f)
                                    )
                                }
                                KeylineOverlayMode.SQUIRCLE -> {
                                    val cornerRadius = safeRadius * 2f * 0.35f
                                    val rect = Rect(centerX - safeRadius, centerY - safeRadius, centerX + safeRadius, centerY + safeRadius)
                                    val maskPath = Path().apply {
                                        fillType = PathFillType.EvenOdd
                                        addRect(Rect(0f, 0f, w, h))
                                        addRoundRect(androidx.compose.ui.geometry.RoundRect(rect, androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)))
                                    }
                                    drawPath(maskPath, color = Color(0x330F172A))
                                    drawRoundRect(
                                        color = PrimaryBlue,
                                        topLeft = Offset(rect.left, rect.top),
                                        size = Size(rect.width, rect.height),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
                                        style = Stroke(width = 2f)
                                    )
                                }
                                KeylineOverlayMode.CIRCLE -> {
                                    val maskPath = Path().apply {
                                        fillType = PathFillType.EvenOdd
                                        addRect(Rect(0f, 0f, w, h))
                                        addOval(Rect(centerX - safeRadius, centerY - safeRadius, centerX + safeRadius, centerY + safeRadius))
                                    }
                                    drawPath(maskPath, color = Color(0x330F172A))
                                    drawCircle(
                                        color = PrimaryBlue,
                                        radius = safeRadius,
                                        center = Offset(centerX, centerY),
                                        style = Stroke(width = 2f)
                                    )
                                }
                                KeylineOverlayMode.NONE -> {}
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Zoom Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zoom",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate800
                        )
                    )

                    Text(
                        text = "${(localZoom * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate800
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newZoom = (localZoom - 0.1f).coerceIn(0.2f, 3.0f)
                            localZoom = newZoom
                            onZoomChange(newZoom)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .testTag("zoom_minus_button")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = Slate800, modifier = Modifier.size(18.dp))
                    }

                    Slider(
                        value = localZoom,
                        onValueChange = {
                            localZoom = it
                            onZoomChange(it)
                        },
                        valueRange = 0.2f..3.0f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .testTag("zoom_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryBlue,
                            activeTrackColor = PrimaryBlue,
                            inactiveTrackColor = Color(0xFFDCE6FF)
                        )
                    )

                    IconButton(
                        onClick = {
                            val newZoom = (localZoom + 0.1f).coerceIn(0.2f, 3.0f)
                            localZoom = newZoom
                            onZoomChange(newZoom)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .testTag("zoom_plus_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Slate800, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions: Reset, Auto Fit, Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            localZoom = 1.0f
                            localPanX = 0f
                            localPanY = 0f
                            onReset()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("reset_density_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Slate300),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Slate800)
                    ) {
                        Text("Reset", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            localZoom = 1.0f
                            localPanX = 0f
                            localPanY = 0f
                            onZoomChange(1.0f)
                            onPanChange(0f, 0f)
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("auto_safe_fit_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFDCE6FF)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = PrimaryBlueLight, contentColor = PrimaryBlue)
                    ) {
                        Text("Auto Fit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_density_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
