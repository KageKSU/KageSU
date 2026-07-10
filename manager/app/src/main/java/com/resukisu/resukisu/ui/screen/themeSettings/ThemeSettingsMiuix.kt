package com.resukisu.resukisu.ui.screen.themeSettings

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.resukisu.resukisu.R
import com.resukisu.resukisu.ui.component.settings.SegmentedColumn
import com.resukisu.resukisu.ui.theme.LocalEnableBlur
import com.resukisu.resukisu.ui.util.BlurredBar
import com.resukisu.resukisu.ui.util.rememberBlurBackdrop
import com.resukisu.resukisu.ui.viewmodel.HomeUiState
import com.resukisu.resukisu.ui.viewmodel.HomeViewModel
import com.resukisu.resukisu.ui.viewmodel.ModuleUiState
import com.resukisu.resukisu.ui.viewmodel.ModuleViewModel
import com.resukisu.resukisu.ui.viewmodel.PredictiveBackAnimation
import com.resukisu.resukisu.ui.viewmodel.SettingsUiState
import com.resukisu.resukisu.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * Miuix chrome for ReSukiSU's Theme screen. Reuses the exact same content
 * composables as the Material screen (AppearanceSettings / predictive-back /
 * CustomizationSettings) — those already adapt to Miuix internally (e.g. the
 * custom-background block is hidden, the blur/floating-bar toggles are shown) —
 * and only wraps them in a miuix Scaffold + collapsing TopAppBar + blur + the
 * scroll-end haptic. The wallpaper/blur content logic is deliberately untouched.
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
                    AppearanceSettings(
                        state = settingsState,
                        viewModel = settingsViewModel,
                        pickImageLauncher = pickImageLauncher,
                        coroutineScope = coroutineScope
                    )
                }

                item {
                    val transition = LocalNavAnimatedContentScope.current.transition
                    SegmentedColumn(
                        title = stringResource(R.string.predictive_back_settings)
                    ) {
                        item {
                            PredictiveBackAnimationWidget(settingsState) { animation ->
                                transition.setPlaytimeAfterInitialAndTargetStateEstablished(
                                    transition.targetState,
                                    transition.targetState,
                                    transition.playTimeNanos
                                )
                                settingsViewModel.setPredictiveBackAnimation(context, animation)
                            }
                        }
                        item(
                            visible = settingsState.predictiveBackAnimation == PredictiveBackAnimation.Scale ||
                                    settingsState.predictiveBackAnimation == PredictiveBackAnimation.AOSP
                        ) {
                            PredictiveBackAnimationDirectionWidget(settingsState) { direction ->
                                settingsViewModel.setPredictiveBackExitDirection(context, direction)
                            }
                        }
                    }
                }

                item {
                    CustomizationSettings(
                        homeUiState = homeUiState,
                        moduleUiState = moduleUiState,
                        settingsUiState = settingsState,
                        settingsViewModel = settingsViewModel,
                        homeViewModel = homeViewModel,
                        moduleViewModel = moduleViewModel
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
