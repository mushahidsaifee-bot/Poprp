package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BackgroundMode
import com.example.model.GeneratorSettings
import com.example.model.IconShape
import com.example.model.OutputFormat
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate800

@Composable
fun SettingsScreen(
    currentSettings: GeneratorSettings,
    onBack: () -> Unit,
    onSaveSettings: (String, OutputFormat, BackgroundMode, Long, IconShape) -> Unit,
    modifier: Modifier = Modifier
) {
    var resName by remember(currentSettings.resourceName) { mutableStateOf(currentSettings.resourceName) }
    var selectedFormat by remember(currentSettings.outputFormat) { mutableStateOf(currentSettings.outputFormat) }
    var backgroundMode by remember(currentSettings.backgroundMode) { mutableStateOf(currentSettings.backgroundMode) }
    var solidColor by remember(currentSettings.solidColor) { mutableLongStateOf(currentSettings.solidColor) }
    var selectedShape by remember(currentSettings.iconShape) { mutableStateOf(currentSettings.iconShape) }

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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Slate800,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Slate800
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 1: Resource Name
                Text(
                    text = "Resource Name",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate800
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = resName,
                    onValueChange = { resName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resource_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = PrimaryBlue,
                        unfocusedIndicatorColor = Slate200
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Section 2: Icon Shape
                Text(
                    text = "Icon Shape",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate800
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val shapes = listOf(
                        IconShape.ROUNDED_SQUARE to "Rounded",
                        IconShape.CIRCLE to "Circle",
                        IconShape.SQUIRCLE to "Squircle",
                        IconShape.ORIGINAL to "Original"
                    )
                    for ((shape, label) in shapes) {
                        ShapeChip(
                            label = label,
                            selected = selectedShape == shape,
                            onClick = { selectedShape = shape },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Section 3: Output Format
                Text(
                    text = "Output Format",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate800
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ShapeChip(
                        label = "PNG",
                        selected = selectedFormat == OutputFormat.PNG,
                        onClick = { selectedFormat = OutputFormat.PNG },
                        modifier = Modifier.weight(1f)
                    )
                    ShapeChip(
                        label = "JPG",
                        selected = selectedFormat == OutputFormat.JPG,
                        onClick = { selectedFormat = OutputFormat.JPG },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Section 4: Background
                Text(
                    text = "Background",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate800
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { backgroundMode = BackgroundMode.TRANSPARENT }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = backgroundMode == BackgroundMode.TRANSPARENT,
                        onClick = { backgroundMode = BackgroundMode.TRANSPARENT },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Transparent",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate800
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { backgroundMode = BackgroundMode.SOLID_COLOR }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = backgroundMode == BackgroundMode.SOLID_COLOR,
                        onClick = { backgroundMode = BackgroundMode.SOLID_COLOR },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Solid Color",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate800
                        )
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(solidColor))
                            .border(BorderStroke(1.5.dp, Slate300), CircleShape)
                    )
                }

                if (backgroundMode == BackgroundMode.SOLID_COLOR) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val colors = listOf(
                            0xFFFFFFFFL to "White",
                            0xFF0F172AL to "Dark",
                            0xFF3B5BFDL to "Blue",
                            0xFF3DDC84L to "Green",
                            0xFFEF4444L to "Red"
                        )
                        for ((c, name) in colors) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .border(
                                        BorderStroke(
                                            if (solidColor == c) 2.5.dp else 1.dp,
                                            if (solidColor == c) PrimaryBlue else Slate300
                                        ),
                                        CircleShape
                                    )
                                    .clickable { solidColor = c },
                                contentAlignment = Alignment.Center
                            ) {
                                if (solidColor == c) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = name,
                                        tint = if (c == 0xFFFFFFFFL) PrimaryBlue else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Save Button
                Button(
                    onClick = {
                        onSaveSettings(resName, selectedFormat, backgroundMode, solidColor, selectedShape)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_settings_button"),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Save Settings", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun ShapeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) PrimaryBlue else Color.White)
            .border(
                BorderStroke(1.dp, if (selected) PrimaryBlue else Slate300),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else Slate800,
                fontSize = 12.sp
            )
        )
    }
}
