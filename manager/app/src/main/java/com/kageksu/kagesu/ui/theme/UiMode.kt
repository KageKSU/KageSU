package com.kageksu.kagesu.ui.theme

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.kageksu.kagesu.KernelSUApplication

/**
 * Which UI system the manager renders with — ported from SukiSU. Miuix is the
 * stock design (chosen on a fresh install); Material is the upstream ReSukiSU
 * UI, left untouched. Persisted in app preferences and exposed via [LocalUiMode].
 */
enum class UiMode(val value: String) {
    Miuix("miuix"),
    Material("material");

    companion object {
        const val PREF_KEY = "ui_mode"
        val DEFAULT = Miuix

        fun fromValue(value: String?): UiMode = when (value) {
            Material.value -> Material
            else -> Miuix
        }
    }
}

val LocalUiMode = staticCompositionLocalOf { UiMode.Miuix }

/** Miuix-only chrome toggles (ported from SukiSU's theme CompositionLocals). */
val LocalEnableBlur = staticCompositionLocalOf { false }
val LocalEnableFloatingBottomBar = staticCompositionLocalOf { false }
val LocalEnableFloatingBottomBarBlur = staticCompositionLocalOf { false }

/**
 * Persistence for the UI-mode / Miuix chrome options. Read once at startup and
 * whenever the Theme page changes them; the values are then provided through the
 * CompositionLocals above.
 */
object UiModeConfig {
    const val PREF_FLOATING_BAR = "miuix_floating_bar"
    const val PREF_FLOATING_BAR_BLUR = "miuix_floating_bar_blur"
    const val PREF_BLUR = "miuix_blur"

    private fun prefs(context: Context) =
        (context.applicationContext as KernelSUApplication).ensurePreferencesRepository()

    fun getUiMode(context: Context): UiMode =
        UiMode.fromValue(prefs(context).getString(UiMode.PREF_KEY, UiMode.DEFAULT.value))

    fun setUiMode(context: Context, mode: UiMode) =
        prefs(context).putString(UiMode.PREF_KEY, mode.value)

    fun getFloatingBar(context: Context) = prefs(context).getBoolean(PREF_FLOATING_BAR, false)
    fun setFloatingBar(context: Context, v: Boolean) = prefs(context).putBoolean(PREF_FLOATING_BAR, v)

    fun getFloatingBarBlur(context: Context) = prefs(context).getBoolean(PREF_FLOATING_BAR_BLUR, false)
    fun setFloatingBarBlur(context: Context, v: Boolean) = prefs(context).putBoolean(PREF_FLOATING_BAR_BLUR, v)

    fun getBlur(context: Context) = prefs(context).getBoolean(PREF_BLUR, false)
    fun setBlur(context: Context, v: Boolean) = prefs(context).putBoolean(PREF_BLUR, v)
}
