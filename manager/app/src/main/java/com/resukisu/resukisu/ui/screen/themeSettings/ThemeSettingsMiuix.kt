package com.resukisu.resukisu.ui.screen.themeSettings

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.resukisu.resukisu.R
import com.resukisu.resukisu.ui.theme.BackgroundManager
import com.resukisu.resukisu.ui.theme.LocalEnableBlur
import com.resukisu.resukisu.ui.theme.ThemeConfig
import com.resukisu.resukisu.ui.util.BlurredBar
import com.resukisu.resukisu.ui.util.rememberBlurBackdrop
import com.resukisu.resukisu.ui.viewmodel.HomeUiState
import com.resukisu.resukisu.ui.viewmodel.HomeViewModel
import com.resukisu.resukisu.ui.viewmodel.ModuleUiState
import com.resukisu.resukisu.ui.viewmodel.ModuleViewModel
import com.resukisu.resukisu.ui.viewmodel.PredictiveBackAnimation
import com.resukisu.resukisu.ui.viewmodel.PredictiveBackExitDirection
import com.resukisu.resukisu.ui.viewmodel.SettingsUiState
import com.resukisu.resukisu.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * Miuix rendering of ReSukiSU's Theme screen. The chrome is a miuix Scaffold +
 * collapsing TopAppBar + blur + scroll-end haptic; the content uses miuix Cards +
 * preference widgets (no MD3 SegmentedColumn cards). The genuinely custom widgets
 * (language row, colour picker, DPI slider) are reused from the Material screen —
 * their callbacks (and the wallpaper/blur logic they never touch) are unchanged.
 */
@SuppressLint("RestrictedApi")
@Composable
internal fun ThemeSettingsScreenMiuix(
    settingsState: SettingsUiState,
    settingsViewModel: SettingsViewModel,
    homeUiState: HomeUiState,
    homeViewModel: HomeViewModel,
    moduleUiState: ModuleUiState,
    moduleViewModel: ModuleViewModel,
    pickImageLauncher: ManagedActivityResultLauncher<String, Uri?>,
    coroutineScope: CoroutineScope,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val enableBlur = LocalEnableBlur.current
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop(enableBlur)
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else colorScheme.surface

    Scaffold(
        topBar = {
            TopBar(
                onBack = onBack,
                scrollBehavior = scrollBehavior,
                backdrop = backdrop,
                barColor = barColor,
            )
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .scrollEndHaptic()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .overScrollVertical(),
                contentPadding = innerPadding,
                overscrollEffect = null,
            ) {
                item {
                    AppearanceSettingsMiuix(
                        state = settingsState,
                        viewModel = settingsViewModel,
                        coroutineScope = coroutineScope,
                    )
                }

                item {
                    val transition = LocalNavAnimatedContentScope.current.transition
                    SmallTitle(text = stringResource(R.string.predictive_back_settings))
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        OverlayDropdownPreference(
                            title = stringResource(R.string.predictive_back_animation),
                            items = listOf(
                                stringResource(R.string.predictive_back_animation_none),
                                stringResource(R.string.predictive_back_animation_aosp),
                                stringResource(R.string.predictive_back_animation_miuix),
                                stringResource(R.string.predictive_back_animation_scale),
                                stringResource(R.string.predictive_back_animation_ksu_classic),
                            ),
                            startAction = { PrefIcon(Icons.Rounded.Animation) },
                            selectedIndex = settingsState.predictiveBackAnimation.ordinal,
                            onSelectedIndexChange = { index ->
                                transition.setPlaytimeAfterInitialAndTargetStateEstablished(
                                    transition.targetState,
                                    transition.targetState,
                                    transition.playTimeNanos
                                )
                                PredictiveBackAnimation.entries.getOrNull(index)?.let {
                                    settingsViewModel.setPredictiveBackAnimation(context, it)
                                }
                            }
                        )
                        if (settingsState.predictiveBackAnimation == PredictiveBackAnimation.Scale ||
                            settingsState.predictiveBackAnimation == PredictiveBackAnimation.AOSP
                        ) {
                            OverlayDropdownPreference(
                                title = stringResource(R.string.predictive_back_exit_direction),
                                items = listOf(
                                    stringResource(R.string.predictive_back_exit_direction_follow_gesture),
                                    stringResource(R.string.predictive_back_exit_direction_always_right),
                                    stringResource(R.string.predictive_back_exit_direction_always_left),
                                ),
                                startAction = { PrefIcon(Icons.Rounded.SwapHoriz) },
                                selectedIndex = settingsState.predictiveBackExitDirection.ordinal,
                                onSelectedIndexChange = { index ->
                                    PredictiveBackExitDirection.entries.getOrNull(index)?.let {
                                        settingsViewModel.setPredictiveBackExitDirection(context, it)
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    CustomizationSettingsMiuix(
                        homeUiState = homeUiState,
                        moduleUiState = moduleUiState,
                        settingsUiState = settingsState,
                        settingsViewModel = settingsViewModel,
                        homeViewModel = homeViewModel,
                        moduleViewModel = moduleViewModel,
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 16.dp))
                }
            }
        }
    }
}

@Composable
private fun AppearanceSettingsMiuix(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    coroutineScope: CoroutineScope,
) {
    val context = LocalContext.current
    SmallTitle(text = stringResource(R.string.appearance_settings))

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        // Reused language row (opens its own dialog); callback unchanged.
        LanguageSetting(state = state, viewModel = viewModel)
    }

    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp)) {
        OverlayDropdownPreference(
            title = stringResource(R.string.theme_mode),
            items = state.themeOptions,
            startAction = { PrefIcon(Icons.Filled.DarkMode) },
            selectedIndex = state.themeMode,
            onSelectedIndexChange = { index -> viewModel.handleThemeModeChange(context, index) }
        )
        SwitchPreference(
            title = stringResource(R.string.dynamic_color_title),
            summary = stringResource(R.string.dynamic_color_summary),
            startAction = { PrefIcon(Icons.Filled.ColorLens) },
            checked = state.useDynamicColor,
            onCheckedChange = { viewModel.handleDynamicColorChange(context, it) }
        )
    }

    if (!state.useDynamicColor) {
        Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp)) {
            ThemeColorSelection(viewModel = viewModel)
        }
    }

    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp)) {
        OverlayDropdownPreference(
            title = stringResource(R.string.dynamic_palette_style),
            items = PaletteStyle.entries.map { it.displayName() },
            startAction = { PrefIcon(Icons.Filled.Style) },
            selectedIndex = PaletteStyle.entries.indexOf(state.dynamicPaletteStyle),
            onSelectedIndexChange = { index ->
                viewModel.handleDynamicPaletteStyleChange(
                    context,
                    PaletteStyle.entries.getOrElse(index) { PaletteStyle.TonalSpot }
                )
            }
        )
        OverlayDropdownPreference(
            title = stringResource(R.string.dynamic_color_spec),
            items = ColorSpec.SpecVersion.entries.map { it.displayName() },
            startAction = { PrefIcon(Icons.Filled.DesignServices) },
            selectedIndex = ColorSpec.SpecVersion.entries.indexOf(state.dynamicColorSpec),
            onSelectedIndexChange = { index ->
                viewModel.handleDynamicColorSpecChange(
                    context,
                    ColorSpec.SpecVersion.entries.getOrElse(index) { ColorSpec.SpecVersion.SPEC_2021 }
                )
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            SmallTitle(text = stringResource(R.string.app_dpi_title))
            DpiSliderControls(state = state, viewModel = viewModel, coroutineScope = coroutineScope)
        }
    }

    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp)) {
        SwitchPreference(
            title = stringResource(R.string.settings_config_enable_blur),
            summary = stringResource(R.string.settings_config_enable_blur_summary),
            startAction = { PrefIcon(Icons.Filled.BlurOn) },
            checked = ThemeConfig.miuixEnableBlur,
            onCheckedChange = { BackgroundManager.saveMiuixEnableBlur(context, it) }
        )
        SwitchPreference(
            title = stringResource(R.string.settings_floating_bottom_bar),
            summary = stringResource(R.string.settings_floating_bottom_bar_summary),
            startAction = { PrefIcon(Icons.Filled.Style) },
            checked = ThemeConfig.enableFloatingBottomBar,
            onCheckedChange = { isChecked ->
                BackgroundManager.saveEnableFloatingBottomBar(context, isChecked)
                if (!isChecked) BackgroundManager.saveEnableFloatingBottomBarBlur(context, false)
            }
        )
        if (ThemeConfig.enableFloatingBottomBar) {
            SwitchPreference(
                title = stringResource(R.string.settings_floating_bottom_bar_blur),
                summary = stringResource(R.string.settings_floating_bottom_bar_blur_summary),
                startAction = { PrefIcon(Icons.Filled.Opacity) },
                checked = ThemeConfig.enableFloatingBottomBarBlur,
                onCheckedChange = { BackgroundManager.saveEnableFloatingBottomBarBlur(context, it) }
            )
        }
    }
}

@Composable
private fun CustomizationSettingsMiuix(
    homeUiState: HomeUiState,
    moduleUiState: ModuleUiState,
    settingsUiState: SettingsUiState,
    settingsViewModel: SettingsViewModel,
    homeViewModel: HomeViewModel,
    moduleViewModel: ModuleViewModel,
) {
    val context = LocalContext.current
    SmallTitle(text = stringResource(R.string.custom_settings))
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        SwitchPreference(
            title = stringResource(R.string.icon_switch_title),
            summary = stringResource(R.string.icon_switch_summary),
            startAction = { PrefIcon(Icons.Filled.Android) },
            checked = settingsUiState.useAltIcon,
            onCheckedChange = { settingsViewModel.handleIconChange(context, it) }
        )
        SwitchPreference(
            title = stringResource(R.string.show_more_module_info),
            summary = stringResource(R.string.show_more_module_info_summary),
            startAction = { PrefIcon(Icons.Filled.Info) },
            checked = moduleUiState.showMoreModuleInfo,
            onCheckedChange = { moduleViewModel.handleShowMoreModuleInfoChange(context, it) }
        )
        SwitchPreference(
            title = stringResource(R.string.simple_mode),
            summary = stringResource(R.string.simple_mode_summary),
            startAction = { PrefIcon(Icons.Filled.Brush) },
            checked = homeUiState.isSimpleMode,
            onCheckedChange = { homeViewModel.handleSimpleModeChange(context, it) }
        )
    }

    Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp)) {
        HideSwitch(R.string.hide_kernel_kernelsu_version, R.string.hide_kernel_kernelsu_version_summary, homeUiState.isHideVersion, homeViewModel::handleHideVersionChange)
        HideSwitch(R.string.hide_other_info, R.string.hide_other_info_summary, homeUiState.isHideOtherInfo, homeViewModel::handleHideOtherInfoChange)
        HideSwitch(R.string.hide_susfs_status, R.string.hide_susfs_status_summary, homeUiState.isHideSusfsStatus, homeViewModel::handleHideSusfsStatusChange)
        HideSwitch(R.string.hide_zygisk_implement, R.string.hide_zygisk_implement_summary, homeUiState.isHideZygiskImplement, homeViewModel::handleHideZygiskImplementChange)
        HideSwitch(R.string.hide_meta_module_implement, R.string.hide_meta_module_implement_summary, homeUiState.isHideMetaModuleImplement, homeViewModel::handleHideMetaModuleImplementChange)
        HideSwitch(R.string.hide_link_card, R.string.hide_link_card_summary, homeUiState.isHideLinkCard, homeViewModel::handleHideLinkCardChange)
        HideSwitch(R.string.hide_tag_card, R.string.hide_tag_card_summary, moduleUiState.isHideTagRow, moduleViewModel::handleHideTagRowChange)
    }
}

@Composable
private fun HideSwitch(
    titleRes: Int,
    summaryRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SwitchPreference(
        title = stringResource(titleRes),
        summary = stringResource(summaryRes),
        startAction = { PrefIcon(Icons.Filled.VisibilityOff) },
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Composable
private fun PrefIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        modifier = Modifier.padding(end = 6.dp),
        tint = colorScheme.onBackground,
        contentDescription = null,
    )
}

@Composable
private fun TopBar(
    onBack: () -> Unit,
    scrollBehavior: ScrollBehavior,
    backdrop: LayerBackdrop?,
    barColor: Color,
) {
    BlurredBar(backdrop) {
        TopAppBar(
            color = barColor,
            title = stringResource(R.string.theme_settings),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    val layoutDirection = LocalLayoutDirection.current
                    Icon(
                        modifier = Modifier.graphicsLayer {
                            if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                        },
                        imageVector = MiuixIcons.Back,
                        tint = colorScheme.onSurface,
                        contentDescription = null,
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
    }
}
