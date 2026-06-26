package com.kageksu.kagesu.ui.util

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.takeOrElse
import com.kageksu.kagesu.ui.theme.LocalContentSurfaceColor
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Blur radius for a Miuix top bar. The bar must behave exactly like it does
 *  without a wallpaper: a constant frosted blur whenever blur is available (it is
 *  applied even at the top of the list, not ramped in on scroll), and none when
 *  blur is off. The extra params are kept for call-site symmetry. */
@Suppress("UNUSED_PARAMETER")
fun wallpaperBarBlur(
    blurAvailable: Boolean,
    wallpaperActive: Boolean,
    scrollBehavior: ScrollBehavior,
): Float = if (blurAvailable) 25f else 0f

/** Background color for a Miuix top bar over a wallpaper. When blur frosts the bar
 *  it stays transparent so the blur shows through; otherwise it uses the real opaque
 *  surface (provided via [LocalContentSurfaceColor] under a wallpaper) so the bar is
 *  a solid surface bar everywhere, just like without a wallpaper. */
@Composable
fun wallpaperBarColor(blurActive: Boolean): Color =
    if (blurActive) Color.Transparent
    else LocalContentSurfaceColor.current.takeOrElse { MiuixTheme.colorScheme.surface }

@Composable
fun rememberBlurBackdrop(enableBlur: Boolean): LayerBackdrop? {
    if (!enableBlur || !isRenderEffectSupported()) return null
    // Use the real opaque surface (provided when a wallpaper is active) so the
    // blur layer never captures a transparent surface and renders black.
    val surfaceColor = LocalContentSurfaceColor.current.takeOrElse { MiuixTheme.colorScheme.surface }
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

@Composable
fun BlurredBar(
    backdrop: LayerBackdrop?,
    blurRadius: Float = 25f,
    content: @Composable () -> Unit,
) {
    val surfaceColor = LocalContentSurfaceColor.current.takeOrElse { MiuixTheme.colorScheme.surface }
    // Tint the bar proportionally to the blur so it fades in smoothly (used when
    // a wallpaper ramps the blur with scroll instead of toggling it on/off).
    val tintAlpha = 0.87f * (blurRadius / 25f).coerceIn(0f, 1f)
    Box(
        modifier = if (blurRadius > 0.5f && backdrop != null) {
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = blurRadius,
                colors = BlurColors(
                    blendColors = listOf(
                        BlendColorEntry(color = surfaceColor.copy(tintAlpha)),
                    ),
                ),
            )
        } else {
            Modifier
        },
    ) {
        content()
    }
}