package com.kageksu.kagesu.ui.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val BACKGROUND_FILE_NAME = "custom_background.img"
private const val TAG = "Background"

/** Copies the picked image into app-internal storage so it survives reboots and
 *  does not depend on a (revocable) content-uri permission. Returns the stored
 *  absolute path, or null on failure. Must be called off the main thread. */
fun copyBackgroundImage(context: Context, uri: Uri): String? {
    return try {
        val target = File(context.filesDir, BACKGROUND_FILE_NAME)
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null
        target.absolutePath
    } catch (e: Exception) {
        Log.e(TAG, "failed to copy background image", e)
        null
    }
}

fun deleteBackgroundImage(path: String) {
    if (path.isBlank()) return
    runCatching { File(path).takeIf { it.exists() }?.delete() }
}

/** Decodes the stored background, downsampled to roughly the screen size to keep
 *  memory bounded. Decoding happens on the IO dispatcher; recomposes once ready. */
@Composable
fun rememberBackgroundBitmap(path: String): ImageBitmap? {
    return produceState<ImageBitmap?>(initialValue = null, key1 = path) {
        value = if (path.isBlank()) {
            null
        } else withContext(Dispatchers.IO) {
            decodeDownsampled(path)
        }
    }.value
}

private fun decodeDownsampled(path: String, maxDimension: Int = 1440): ImageBitmap? {
    val file = File(path)
    if (!file.exists()) return null
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        val largest = maxOf(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)
        var sample = 1
        while (largest / sample > maxDimension) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        BitmapFactory.decodeFile(path, opts)?.asImageBitmap()
    } catch (e: Exception) {
        Log.e(TAG, "failed to decode background image", e)
        null
    }
}
