package com.kageksu.kagesu.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import com.kageksu.kagesu.ui.webui.MonetColorsProvider
import com.materialkolor.dynamiccolor.ColorSpec
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

/**
 * Top-level entry (SukiSU pattern): reads the persisted UI mode + Miuix chrome
 * options, provides them as CompositionLocals, and dispatches to the Miuix or
 * Material theme. Drop-in replacement for the previous `KernelSUTheme { }` call.
 */
/** No-arg overload used by ported SukiSU Miuix components. */
@Composable
@ReadOnlyComposable
fun isInDarkTheme(): Boolean = isInDarkTheme(ThemeConfig.forceDarkMode)

@Composable
fun KageSURoot(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val uiMode = remember { UiModeConfig.getUiMode(context) }
    val enableBlur = remember { UiModeConfig.getBlur(context) }
    val enableFloating = remember { UiModeConfig.getFloatingBar(context) }
    val enableFloatingBlur = remember { UiModeConfig.getFloatingBarBlur(context) }

    CompositionLocalProvider(
        LocalUiMode provides uiMode,
        LocalEnableBlur provides enableBlur,
        LocalEnableFloatingBottomBar provides enableFloating,
        LocalEnableFloatingBottomBarBlur provides enableFloatingBlur,
    ) {
        when (uiMode) {
            UiMode.Miuix -> MiuixKernelSUTheme(content = content)
            UiMode.Material -> KernelSUTheme(content = content)
        }
    }
}

/**
 * Miuix theme, reading the same [ThemeConfig] as the Material theme so dark mode,
 * dynamic color and seed color stay consistent between the two front-ends.
 */
@Composable
fun MiuixKernelSUTheme(
    darkTheme: Boolean = isInDarkTheme(ThemeConfig.forceDarkMode),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val dynamic = ThemeConfig.useDynamicColor

    val colorSchemeMode = when (ThemeConfig.forceDarkMode) {
        null -> if (dynamic) ColorSchemeMode.MonetSystem else ColorSchemeMode.System
        true -> if (dynamic) ColorSchemeMode.MonetDark else ColorSchemeMode.Dark
        false -> if (dynamic) ColorSchemeMode.MonetLight else ColorSchemeMode.Light
    }

    val paletteStyle = try {
        ThemePaletteStyle.valueOf(ThemeConfig.dynamicPaletteStyle.name)
    } catch (_: Exception) {
        ThemePaletteStyle.TonalSpot
    }

    val colorSpec = if (ThemeConfig.dynamicColorSpec == ColorSpec.SpecVersion.SPEC_2025) {
        ThemeColorSpec.Spec2025
    } else {
        ThemeColorSpec.Spec2021
    }

    val controller = ThemeController(
        colorSchemeMode,
        keyColor = if (dynamic) null else Color(ThemeConfig.seedColor),
        isDark = darkTheme,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
    )

    // Safety net: provide a Material color scheme too, so any screen not yet
    // ported to Miuix still renders themed (not with broken/uninitialized colors)
    // while the Miuix screen transplant is in progress.
    MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
    MiuixTheme(
        controller = controller,
        content = {
            LaunchedEffect(darkTheme) {
                val window = (context as? Activity)?.window ?: return@LaunchedEffect
                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            MonetColorsProvider.UpdateCss()
            CompositionLocalProvider(
                LocalContentColor provides MiuixTheme.colorScheme.onBackground,
            ) {
                content()
            }
        }
    )
    }
}
