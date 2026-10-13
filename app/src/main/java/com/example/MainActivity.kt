package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.DensityConfig
import com.example.model.MipmapDensity
import com.example.ui.screens.AllPreviewsScreen
import com.example.ui.screens.ConfirmGenerateScreen
import com.example.ui.screens.DensityEditorScreen
import com.example.ui.screens.DownloadCompleteScreen
import com.example.ui.screens.GeneratingZipScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImageUploadScreen
import com.example.ui.screens.ProcessingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.ZipStructurePreviewScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MipmapViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MipmapApp()
            }
        }
    }
}

@Composable
fun MipmapApp(
    viewModel: MipmapViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Initialize sample asset on first run
    LaunchedEffect(Unit) {
        viewModel.initDefaultImage(context)
    }

    // Hardware BackHandler routing
    BackHandler(enabled = uiState.currentScreen != AppScreen.SPLASH && uiState.currentScreen != AppScreen.HOME) {
        when (uiState.currentScreen) {
            AppScreen.IMAGE_UPLOAD -> viewModel.navigateTo(AppScreen.HOME)
            AppScreen.PROCESSING -> viewModel.navigateTo(AppScreen.IMAGE_UPLOAD)
            AppScreen.ALL_PREVIEWS -> viewModel.navigateTo(AppScreen.HOME)
            AppScreen.DENSITY_EDITOR -> viewModel.navigateTo(AppScreen.ALL_PREVIEWS)
            AppScreen.SETTINGS -> viewModel.navigateTo(AppScreen.HOME)
            AppScreen.CONFIRM_GENERATE -> viewModel.navigateTo(AppScreen.ALL_PREVIEWS)
            AppScreen.GENERATING_ZIP -> viewModel.navigateTo(AppScreen.CONFIRM_GENERATE)
            AppScreen.DOWNLOAD_COMPLETE -> viewModel.navigateTo(AppScreen.HOME)
            AppScreen.ZIP_STRUCTURE_PREVIEW -> viewModel.navigateTo(AppScreen.DOWNLOAD_COMPLETE)
            else -> {}
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = uiState.currentScreen == AppScreen.HOME || uiState.currentScreen == AppScreen.ALL_PREVIEWS,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                // Header in Drawer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Mipmaps",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Mipmap Generator",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate800,
                            fontSize = 17.sp
                        )
                    )
                }

                HorizontalDivider(color = Slate200)

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("Home / Dashboard") },
                    selected = uiState.currentScreen == AppScreen.HOME,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.navigateTo(AppScreen.HOME)
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryBlueLight,
                        selectedTextColor = PrimaryBlue,
                        selectedIconColor = PrimaryBlue
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Select Image") },
                    selected = uiState.currentScreen == AppScreen.IMAGE_UPLOAD,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.navigateTo(AppScreen.IMAGE_UPLOAD)
                    },
                    icon = { Icon(Icons.Default.Image, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryBlueLight,
                        selectedTextColor = PrimaryBlue,
                        selectedIconColor = PrimaryBlue
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Mipmap Previews") },
                    selected = uiState.currentScreen == AppScreen.ALL_PREVIEWS,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.navigateTo(AppScreen.ALL_PREVIEWS)
                    },
                    icon = { Icon(Icons.Default.ViewModule, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryBlueLight,
                        selectedTextColor = PrimaryBlue,
                        selectedIconColor = PrimaryBlue
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = uiState.currentScreen == AppScreen.SETTINGS,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.navigateTo(AppScreen.SETTINGS)
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryBlueLight,
                        selectedTextColor = PrimaryBlue,
                        selectedIconColor = PrimaryBlue
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp), color = Slate200)

                NavigationDrawerItem(
                    label = { Text("Reset All Densities") },
                    selected = false,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        for (d in MipmapDensity.entries) {
                            viewModel.resetDensityConfig(d)
                        }
                    },
                    icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier
                .fillMaxSize()
                .testTag("app_root_scaffold")
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentScreen) {
                    AppScreen.SPLASH -> {
                        SplashScreen(
                            onTimeout = { viewModel.navigateTo(AppScreen.HOME) }
                        )
                    }

                    AppScreen.HOME -> {
                        HomeScreen(
                            onStartNow = { viewModel.navigateTo(AppScreen.IMAGE_UPLOAD) },
                            onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            onOpenMenu = { coroutineScope.launch { drawerState.open() } }
                        )
                    }

                    AppScreen.IMAGE_UPLOAD -> {
                        ImageUploadScreen(
                            currentBitmap = uiState.sourceBitmap,
                            fileName = uiState.imageFileName,
                            width = uiState.imageWidth,
                            height = uiState.imageHeight,
                            fileSizeBytes = uiState.imageFileSize,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onImageSelected = { uri -> viewModel.onImagePicked(context, uri) },
                            onBitmapLoaded = { bitmap, name -> viewModel.onBitmapLoaded(bitmap, name) },
                            onContinueProcessing = { viewModel.startProcessingWorkflow() }
                        )
                    }

                    AppScreen.PROCESSING -> {
                        ProcessingScreen(
                            currentBitmap = uiState.sourceBitmap,
                            progress = uiState.processingProgress
                        )
                    }

                    AppScreen.ALL_PREVIEWS -> {
                        AllPreviewsScreen(
                            renderedBitmaps = uiState.renderedBitmaps,
                            densityConfigs = uiState.densityConfigs,
                            onBack = { viewModel.navigateTo(AppScreen.IMAGE_UPLOAD) },
                            onOpenMenu = { coroutineScope.launch { drawerState.open() } },
                            onSelectDensity = { density -> viewModel.openDensityEditor(density) },
                            onGenerateZip = { viewModel.navigateTo(AppScreen.CONFIRM_GENERATE) }
                        )
                    }

                    AppScreen.DENSITY_EDITOR -> {
                        val density = uiState.selectedDensityForEdit
                        val config = uiState.densityConfigs[density] ?: DensityConfig(density)
                        DensityEditorScreen(
                            density = density,
                            sourceBitmap = uiState.sourceBitmap,
                            config = config,
                            onBack = { viewModel.navigateTo(AppScreen.ALL_PREVIEWS) },
                            onZoomChange = { zoom -> viewModel.updateDensityZoom(density, zoom) },
                            onPanChange = { panX, panY -> viewModel.updateDensityPan(density, panX, panY) },
                            onReset = { viewModel.resetDensityConfig(density) },
                            onSave = { viewModel.saveDensityEditor(density) }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            currentSettings = uiState.settings,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onSaveSettings = { name, format, bg, color, shape ->
                                viewModel.updateSettings(name, format, bg, color, shape)
                            }
                        )
                    }

                    AppScreen.CONFIRM_GENERATE -> {
                        ConfirmGenerateScreen(
                            settings = uiState.settings,
                            totalDensities = MipmapDensity.entries.size,
                            onBack = { viewModel.navigateTo(AppScreen.ALL_PREVIEWS) },
                            onConfirmGenerate = { viewModel.generateZip(context) }
                        )
                    }

                    AppScreen.GENERATING_ZIP -> {
                        GeneratingZipScreen(
                            progress = uiState.zipProgress
                        )
                    }

                    AppScreen.DOWNLOAD_COMPLETE -> {
                        DownloadCompleteScreen(
                            zipFile = uiState.generatedZipFile,
                            fileSizeText = uiState.zipFileSizeText,
                            onPreviewFiles = { viewModel.navigateTo(AppScreen.ZIP_STRUCTURE_PREVIEW) },
                            onGenerateAgain = { viewModel.navigateTo(AppScreen.HOME) }
                        )
                    }

                    AppScreen.ZIP_STRUCTURE_PREVIEW -> {
                        ZipStructurePreviewScreen(
                            settings = uiState.settings,
                            onBack = { viewModel.navigateTo(AppScreen.DOWNLOAD_COMPLETE) }
                        )
                    }
                }
            }
        }
    }
}
