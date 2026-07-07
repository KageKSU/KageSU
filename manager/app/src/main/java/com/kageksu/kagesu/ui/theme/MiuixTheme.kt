package com.kageksu.kagesu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
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
    // Reactive: reading these Compose-state values makes theme + chrome update
    // live when the user flips the mode/toggles (no restart, no write race).
    val uiMode = UiModeConfig.uiMode

    CompositionLocalProvider(
        LocalUiMode provides uiMode,
        LocalEnableBlur provides UiModeConfig.enableBlur,
        LocalEnableFloatingBottomBar provides UiModeConfig.floatingBar,
        LocalEnableFloatingBottomBarBlur provides UiModeConfig.floatingBarBlur,
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
    val systemIsDark = isSystemInDarkTheme()

    // Load the persisted theme config (seed color, dynamic-color state, dark mode,
    // palette style/spec, card + custom background). This normally runs inside the
    // Material KernelSUTheme's ThemeInitializer; in Miuix mode that theme is not used,
    // so without this the seed color/dynamic state stay at defaults on every cold
    // start ("colors reset after reopening"). Exactly one theme is active at a time,
    // so this never double-runs.
    ThemeInitializer(context = context, systemIsDark = systemIsDark)

    val dynamic = ThemeConfig.useDynamicColor

    // Always use Monet modes: they are the only ones where miuix's ThemeController
    // honors `keyColor`. The plain System/Light/Dark modes ignore keyColor and paint
    // the fixed default palette, so a custom seed color would have no effect (bug:
    // "can't choose colors for miuix").
    val colorSchemeMode = when (ThemeConfig.forceDarkMode) {
        null -> ColorSchemeMode.MonetSystem
        true -> ColorSchemeMode.MonetDark
        false -> ColorSchemeMode.MonetLight
    }

    // Match Material's color derivation: dynamic-on => seed from the real Android
    // wallpaper palette (like dynamic*ColorScheme); dynamic-off => the chosen seed.
    val resolvedKeyColor: Color = if (dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context).primary
        else dynamicLightColorScheme(context).primary
    } else {
        Color(ThemeConfig.seedColor)
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
        keyColor = resolvedKeyColor,
        isDark = darkTheme,
        paletteStyle = paletteStyle,
        colorSpec = colorSpec,
    )

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
