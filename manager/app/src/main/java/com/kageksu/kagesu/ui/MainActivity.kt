package com.kageksu.kagesu.ui

import androidx.compose.runtime.mutableIntStateOf
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.flow.MutableStateFlow
import com.kageksu.kagesu.Natives
import com.kageksu.kagesu.ui.component.bottombar.BottomBar
import com.kageksu.kagesu.ui.component.bottombar.MainPagerState
import com.kageksu.kagesu.ui.component.bottombar.SideRail
import com.kageksu.kagesu.ui.component.bottombar.rememberMainPagerState
import com.kageksu.kagesu.ui.kernelFlash.KernelFlashScreen
import com.kageksu.kagesu.ui.navigation3.HandleDeepLink
import com.kageksu.kagesu.ui.navigation3.LocalNavigator
import com.kageksu.kagesu.ui.navigation3.Navigator
import com.kageksu.kagesu.ui.navigation3.Route
import com.kageksu.kagesu.ui.navigation3.rememberNavigator
import com.kageksu.kagesu.ui.screen.about.AboutScreen
import com.kageksu.kagesu.ui.screen.appprofile.AppProfileScreen
import com.kageksu.kagesu.ui.screen.colorpalette.ColorPaletteScreen
import com.kageksu.kagesu.ui.screen.executemoduleaction.ExecuteModuleActionScreen
import com.kageksu.kagesu.ui.screen.flash.FlashScreen
import com.kageksu.kagesu.ui.screen.home.HomePager
import com.kageksu.kagesu.ui.screen.install.InstallScreen
import com.kageksu.kagesu.ui.screen.kpm.KpmScreen
import com.kageksu.kagesu.ui.screen.module.ModulePager
import com.kageksu.kagesu.ui.screen.modulerepo.ModuleRepoDetailScreen
import com.kageksu.kagesu.ui.screen.modulerepo.ModuleRepoScreen
import com.kageksu.kagesu.ui.screen.settings.SettingPager
import com.kageksu.kagesu.ui.screen.settings.tools.ToolsScreen
import com.kageksu.kagesu.ui.screen.sulog.SulogScreen
import com.kageksu.kagesu.ui.screen.superuser.SuperUserPager
import com.kageksu.kagesu.ui.screen.susfs.SuSFSScreen
import com.kageksu.kagesu.ui.screen.template.AppProfileTemplateScreen
import com.kageksu.kagesu.ui.screen.templateeditor.TemplateEditorScreen
import com.kageksu.kagesu.ui.screen.umountmanager.UmountManagerScreen
import com.kageksu.kagesu.ui.theme.KernelSUTheme
import com.kageksu.kagesu.ui.theme.LocalColorMode
import com.kageksu.kagesu.ui.theme.LocalContentSurfaceColor
import com.kageksu.kagesu.ui.theme.LocalEnableBlur
import com.kageksu.kagesu.ui.theme.LocalEnableFloatingBottomBar
import com.kageksu.kagesu.ui.theme.LocalEnableFloatingBottomBarBlur
import com.kageksu.kagesu.ui.util.install
import com.kageksu.kagesu.ui.util.rememberBackgroundBitmap
import com.kageksu.kagesu.ui.util.rememberBlurBackdrop
import com.kageksu.kagesu.ui.util.rememberContentReady
import com.kageksu.kagesu.ui.util.rootAvailable
import com.kageksu.kagesu.ui.viewmodel.MainActivityViewModel
import com.kageksu.kagesu.ui.viewmodel.MainPagerConfig
import com.kageksu.kagesu.ui.webui.WebUIActivity
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val KEY_INTENT_STATE = "intent_state"

class MainActivity : ComponentActivity() {

    private var intentStateValue by mutableIntStateOf(0)
    private val intentStateFlow = MutableStateFlow(0)
    private val intentState: MutableStateFlow<Int>
        get() {
            if (intentStateFlow.value != intentStateValue) {
                intentStateFlow.value = intentStateValue
            }
            return intentStateFlow
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentStateValue = savedInstanceState?.getInt(KEY_INTENT_STATE, 0) ?: 0
        intentStateFlow.value = intentStateValue

        val isManager = Natives.isManager
        if (isManager && !Natives.requireNewKernel()) install()

        setContent {
            val viewModel = viewModel<MainActivityViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val selectedMainPage by viewModel.selectedMainPage.collectAsStateWithLifecycle()
            val appSettings = uiState.appSettings
            val uiMode = uiState.uiMode
            val darkMode = appSettings.colorMode.isDark || (appSettings.colorMode.isSystem && isSystemInDarkTheme())

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                )
                window.isNavigationBarContrastEnforced = false
                onDispose { }
            }

            val navigator = rememberNavigator(Route.Main)
            val systemDensity = LocalDensity.current
            val density = remember(systemDensity, uiState.pageScale) {
                Density(systemDensity.density * uiState.pageScale, systemDensity.fontScale)
            }

            // Decode the wallpaper once here, then share it with every page via
            // LocalWallpaper so navigating between pages does not re-decode/flicker.
            val wallpaperEnabled = uiState.backgroundEnabled && uiState.backgroundPath.isNotBlank()
            val wallpaperBitmap = if (wallpaperEnabled) rememberBackgroundBitmap(uiState.backgroundPath) else null

            CompositionLocalProvider(
                LocalNavigator provides navigator,
                LocalDensity provides density,
                LocalColorMode provides appSettings.colorMode.value,
                LocalEnableBlur provides uiState.enableBlur,
                LocalEnableFloatingBottomBar provides uiState.enableFloatingBottomBar,
                LocalEnableFloatingBottomBarBlur provides uiState.enableFloatingBottomBarBlur,
                LocalUiMode provides uiMode,
                LocalWallpaper provides WallpaperState(
                    enabled = wallpaperEnabled && wallpaperBitmap != null,
                    bitmap = wallpaperBitmap,
                    dim = uiState.backgroundDim,
                    blurEnabled = uiState.backgroundBlurEnabled,
                    blurRadius = uiState.backgroundBlurRadius,
                ),
            ) {
                KernelSUTheme(appSettings = appSettings, uiMode = uiMode) {
                    HandleDeepLink(intentState = intentState.collectAsStateWithLifecycle())
                    ShortcutIntentHandler(intentState = intentState)
                    HandleZipFileIntent(intentState = intentState)
                    val mainScreenEntry = @Composable {
                        MainScreen(
                            initialPage = selectedMainPage,
                            onPageChanged = viewModel::setSelectedMainPage,
                        )
                    }

                    val navDisplay = @Composable {
                        NavDisplay(
                            backStack = navigator.backStack,
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator()
                            ),
                            onBack = {
                                when (val top = navigator.current()) {
                                    is Route.TemplateEditor -> {
                                        if (!top.readOnly) {
                                            navigator.setResult("template_edit", true)
                                        } else {
                                            navigator.pop()
                                        }
                                    }

                                    else -> navigator.pop()
                                }
                            },
                            entryProvider = entryProvider {
                                entry<Route.Main> { WallpaperPage { mainScreenEntry() } }
                                entry<Route.About> { AboutScreen() }
                                entry<Route.Sulog> { WallpaperPage { SulogScreen() } }
                                entry<Route.ColorPalette> { WallpaperPage { ColorPaletteScreen() } }
                                entry<Route.AppProfileTemplate> { WallpaperPage { AppProfileTemplateScreen() } }
                                entry<Route.TemplateEditor> { key -> WallpaperPage { TemplateEditorScreen(key.template, key.readOnly) } }
                                entry<Route.AppProfile> { key -> WallpaperPage { AppProfileScreen(key.uid) } }
                                entry<Route.ModuleRepo> { WallpaperPage { ModuleRepoScreen() } }
                                entry<Route.ModuleRepoDetail> { key -> WallpaperPage { ModuleRepoDetailScreen(key.module) } }
                                entry<Route.Install> { key -> WallpaperPage { InstallScreen(preselectedKernelUri = key.preselectedKernelUri) } }
                                entry<Route.Flash> { key -> WallpaperPage { FlashScreen(key.flashIt) } }
                                entry<Route.ExecuteModuleAction> { key -> WallpaperPage { ExecuteModuleActionScreen(key.moduleId, key.fromShortcut) } }
                                entry<Route.Home> { WallpaperPage { mainScreenEntry() } }
                                entry<Route.SuperUser> { WallpaperPage { mainScreenEntry() } }
                                entry<Route.Module> { WallpaperPage { mainScreenEntry() } }
                                entry<Route.Settings> { WallpaperPage { mainScreenEntry() } }
                                entry<Route.KernelFlash> { key -> WallpaperPage { KernelFlashScreen(key.kernelUri, key.selectedSlot, key.kpmPatchEnabled, key.kpmUndoPatch) } }
                                entry<Route.Kpm> { WallpaperPage { KpmScreen() } }
                                entry<Route.SuSFS> { WallpaperPage { SuSFSScreen() } }
                                entry<Route.Tool> { WallpaperPage { ToolsScreen() } }
                                entry<Route.UmountManager> { WallpaperPage { UmountManagerScreen() } }
                            }
                        )
                    }

                    when (uiMode) {
                        UiMode.Material -> androidx.compose.material3.Scaffold { navDisplay() }
                        UiMode.Miuix -> Scaffold { navDisplay() }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Increment intentState to trigger LaunchedEffect re-execution
        intentStateValue += 1
        intentStateFlow.value = intentStateValue
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_INTENT_STATE, intentStateValue)
    }
}

val LocalMainPagerState = staticCompositionLocalOf<MainPagerState> { error("LocalMainPagerState not provided") }

@androidx.compose.runtime.Immutable
data class WallpaperState(
    val enabled: Boolean = false,
    val bitmap: androidx.compose.ui.graphics.ImageBitmap? = null,
    val dim: Float = 0.4f,
    val blurEnabled: Boolean = false,
    val blurRadius: Float = 16f,
)

val LocalWallpaper = staticCompositionLocalOf { WallpaperState() }

/** Wraps a navigation entry so the user wallpaper is drawn behind it with a
 *  transparent surface on top. Applied per-entry so each page is self-contained
 *  (opaque wallpaper background), keeping navigation transitions clean. */
@Composable
fun WallpaperPage(content: @Composable () -> Unit) {
    val wallpaper = LocalWallpaper.current
    val bitmap = wallpaper.bitmap
    if (!wallpaper.enabled || bitmap == null) {
        content()
        return
    }
    val uiMode = LocalUiMode.current
    val realSurface = when (uiMode) {
        UiMode.Material -> MaterialTheme.colorScheme.surface
        UiMode.Miuix -> MiuixTheme.colorScheme.surface
    }
    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperBackground(
            bitmap = bitmap,
            dim = wallpaper.dim,
            blurEnabled = wallpaper.blurEnabled,
            blurRadius = wallpaper.blurRadius,
            scrimColor = realSurface,
        )
        CompositionLocalProvider(LocalContentSurfaceColor provides realSurface) {
            WallpaperSurfaceTheme(uiMode) {
                content()
            }
        }
    }
}

/** Re-themes a page with a transparent surface so the wallpaper drawn behind it
 *  shows through, while keeping typography/shapes. */
// Over a wallpaper only `surface` is made transparent so page bodies and top bars
// (Scaffold/TopAppBar use `surface`) reveal the wallpaper. `background` and every
// `surfaceContainer*` stay opaque so cards, the navigation bar, dialogs and bottom
// sheets remain solid/readable — Miuix dialogs (OverlayDialog) use `background`.
@Composable
private fun WallpaperSurfaceTheme(uiMode: UiMode, content: @Composable () -> Unit) {
    val transparent = androidx.compose.ui.graphics.Color.Transparent
    when (uiMode) {
        UiMode.Material -> MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                surface = transparent,
            ),
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes,
            content = content,
        )

        UiMode.Miuix -> MiuixTheme(
            colors = MiuixTheme.colorScheme.copy(
                surface = transparent,
            ),
            content = content,
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    initialPage: Int = 0,
    onPageChanged: (Int) -> Unit = {},
) {
    val navController = LocalNavigator.current
    val enableBlur = LocalEnableBlur.current
    val enableFloatingBottomBar = LocalEnableFloatingBottomBar.current
    val enableFloatingBottomBarBlur = LocalEnableFloatingBottomBarBlur.current
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { MainPagerConfig.PAGE_COUNT })
    val mainPagerState = rememberMainPagerState(pagerState)
    val isManager = Natives.isManager
    val isFullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    var userScrollEnabled by remember(isFullFeatured) { mutableStateOf(isFullFeatured) }
    val uiMode = LocalUiMode.current
    val surfaceColor = when (uiMode) {
        UiMode.Material -> MaterialTheme.colorScheme.surface // Blur is not used in Material, this is just a placeholder
        UiMode.Miuix -> MiuixTheme.colorScheme.surface
    }
    // Under a wallpaper the surface is transparent; use the real opaque surface
    // as the blur backdrop base so the floating bar never renders black.
    val realSurface = LocalContentSurfaceColor.current.takeOrElse { surfaceColor }
    val blurBackdrop = rememberBlurBackdrop(enableBlur)

    val backdrop = rememberLayerBackdrop {
        drawRect(realSurface)
        drawContent()
    }

    val settledPage = mainPagerState.pagerState.settledPage
    LaunchedEffect(settledPage) {
        onPageChanged(settledPage)
    }

    val currentPage = mainPagerState.pagerState.currentPage
    LaunchedEffect(currentPage) {
        mainPagerState.syncPage()
    }

    MainScreenBackHandler(mainPagerState, navController)

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val useNavigationRail = isLandscape && !(uiMode == UiMode.Miuix && enableFloatingBottomBar)

    CompositionLocalProvider(
        LocalMainPagerState provides mainPagerState
    ) {
        val contentReady = rememberContentReady()
        val pagerContent = @Composable { bottomInnerPadding: Dp ->
            Box(modifier = if (blurBackdrop != null) Modifier.layerBackdrop(blurBackdrop) else Modifier) {
                HorizontalPager(
                    modifier = Modifier
                        .then(if (enableFloatingBottomBar && enableFloatingBottomBarBlur) Modifier.layerBackdrop(backdrop) else Modifier),
                    state = mainPagerState.pagerState,
                    beyondViewportPageCount = if (contentReady) 3 else 0,
                    userScrollEnabled = userScrollEnabled,
                ) { page ->
                    val isCurrentPage = page == settledPage
                    when (page) {
                        0 -> if (isCurrentPage || contentReady) HomePager(navController, bottomInnerPadding, isCurrentPage)
                        1 -> if (isCurrentPage || contentReady) SuperUserPager(navController, bottomInnerPadding, isCurrentPage)
                        2 -> if (isCurrentPage || contentReady) ModulePager(bottomInnerPadding, isCurrentPage)
                        3 -> if (isCurrentPage || contentReady) SettingPager(navController, bottomInnerPadding)
                    }
                }
            }
        }

        if (useNavigationRail) {
            val startInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
                .only(WindowInsetsSides.Start)
            val navBarBottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

            when (uiMode) {
                UiMode.Material -> androidx.compose.material3.Scaffold {
                    Row {
                        SideRail(
                            blurBackdrop = blurBackdrop,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(startInsets)
                        ) {
                            pagerContent(navBarBottomPadding)
                        }
                    }
                }

                UiMode.Miuix -> Scaffold { _ ->
                    Row {
                        SideRail(
                            blurBackdrop = blurBackdrop,
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(startInsets)
                        ) {
                            pagerContent(navBarBottomPadding)
                        }
                    }
                }
            }
        } else {
            val bottomBar = @Composable {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BottomBar(
                        blurBackdrop = blurBackdrop,
                        backdrop = backdrop,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }

            when (uiMode) {
                UiMode.Material -> androidx.compose.material3.Scaffold(bottomBar = bottomBar) { innerPadding ->
                    pagerContent(innerPadding.calculateBottomPadding())
                }

                UiMode.Miuix -> Scaffold(bottomBar = bottomBar) { innerPadding ->
                    pagerContent(innerPadding.calculateBottomPadding())
                }
            }
        }
    }
}

@Composable
private fun MainScreenBackHandler(
    mainState: MainPagerState,
    navController: Navigator,
) {
    val isPagerBackHandlerEnabled by remember {
        derivedStateOf {
            navController.current() is Route.Main && navController.backStackSize() == 1 && mainState.selectedPage != 0
        }
    }

    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = isPagerBackHandlerEnabled,
        onBackCompleted = {
            mainState.animateToPage(0)
        }
    )
}

@Composable
private fun WallpaperBackground(
    bitmap: androidx.compose.ui.graphics.ImageBitmap,
    dim: Float,
    blurEnabled: Boolean,
    blurRadius: Float,
    scrimColor: androidx.compose.ui.graphics.Color,
) {
    val base = scrimColor.takeOrElse { androidx.compose.ui.graphics.Color.Black }
    val scrim = base.copy(alpha = dim.coerceIn(0f, 1f))
    val imageModifier = Modifier
        .fillMaxSize()
        .then(
            if (blurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0f)
                Modifier.blur(blurRadius.dp)
            else Modifier
        )
    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = imageModifier,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrim)
        )
    }
}

@Composable
private fun ShortcutIntentHandler(
    intentState: MutableStateFlow<Int>,
) {
    val activity = LocalActivity.current ?: return
    val context = LocalContext.current
    val intentStateValue by intentState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    LaunchedEffect(intentStateValue) {
        val intent = activity.intent
        val type = intent?.getStringExtra("shortcut_type") ?: return@LaunchedEffect

        when (type) {
            "module_action" -> {
                val moduleId = intent.getStringExtra("module_id") ?: return@LaunchedEffect
                navigator.push(Route.ExecuteModuleAction(moduleId, fromShortcut = true))
                intent.removeExtra("shortcut_type")
                intent.removeExtra("module_id")
            }

            "module_webui" -> {
                val moduleId = intent.getStringExtra("module_id") ?: return@LaunchedEffect
                val webIntent = Intent(context, WebUIActivity::class.java)
                    .setData("kernelsu://webui/$moduleId".toUri())
                context.startActivity(webIntent)
            }
            
            else -> return@LaunchedEffect
        }
    }
}