package com.kageksu.kagesu.ui.util

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.kageksu.kagesu.ui.LocalWallpaper
import com.kageksu.kagesu.ui.theme.LocalContentSurfaceColor
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.shader.isRenderEffectSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.max
import kotlin.math.roundToInt

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

/** Draws [bitmap] scaled to cover the whole draw area (ContentScale.Crop), centered. */
internal fun DrawScope.drawWallpaperCropped(bitmap: ImageBitmap) {
    val scale = max(size.width / bitmap.width, size.height / bitmap.height)
    val dstW = (bitmap.width * scale).roundToInt()
    val dstH = (bitmap.height * scale).roundToInt()
    drawImage(
        image = bitmap,
        dstOffset = IntOffset(
            ((size.width - dstW) / 2f).roundToInt(),
            ((size.height - dstH) / 2f).roundToInt(),
        ),
        dstSize = IntSize(dstW, dstH),
    )
}

@Composable
fun rememberBlurBackdrop(enableBlur: Boolean): LayerBackdrop? {
    if (!enableBlur || !isRenderEffectSupported()) return null
    // Use the real opaque surface (provided when a wallpaper is active) so the
    // blur layer never captures a transparent surface and renders black.
    val surfaceColor = LocalContentSurfaceColor.current.takeOrElse { MiuixTheme.colorScheme.surface }
    val wallpaper = LocalWallpaper.current
    val bitmap = wallpaper.bitmap
    return rememberLayerBackdrop {
        // Over a wallpaper the bars frost the wallpaper itself, not a flat surface
        // block: paint the (dimmed) wallpaper as the backdrop base so a frosted bar
        // shows the blurred wallpaper at the top of the list, not a black/white slab.
        if (wallpaper.enabled && bitmap != null) {
            drawRect(surfaceColor)
            drawWallpaperCropped(bitmap)
            if (wallpaper.dim > 0f) {
                drawRect(surfaceColor.copy(alpha = wallpaper.dim))
            }
        } else {
            drawRect(surfaceColor)
        }
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
    // Over a wallpaper the bar is a translucent frosted-glass layer so the blurred
    // wallpaper stays visible through it; without a wallpaper it keeps the heavier
    // surface tint it has always had over scrolled content.
    val wallpaperActive = LocalWallpaper.current.enabled
    val maxTint = if (wallpaperActive) 0.5f else 0.87f
    val tintAlpha = maxTint * (blurRadius / 25f).coerceIn(0f, 1f)
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
