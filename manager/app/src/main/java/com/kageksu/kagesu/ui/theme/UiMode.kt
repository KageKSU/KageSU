package com.kageksu.kagesu.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.kageksu.kagesu.KernelSUApplication

/**
 * Which UI system the manager renders with. Miuix is the stock design (chosen on
 * a fresh install); Material is the upstream ReSukiSU UI, left untouched.
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
val LocalEnableBlur = staticCompositionLocalOf { false }
val LocalEnableFloatingBottomBar = staticCompositionLocalOf { false }
val LocalEnableFloatingBottomBarBlur = staticCompositionLocalOf { false }

/**
 * Reactive holder for the UI-mode / Miuix chrome options. Values are Compose
 * state, so toggling them recomposes the theme + chrome live (no activity
 * restart, no async-write race). Persisted to prefs; [load] restores at startup.
 */
object UiModeConfig {
    private const val PREF_FLOATING_BAR = "miuix_floating_bar"
    private const val PREF_FLOATING_BAR_BLUR = "miuix_floating_bar_blur"
    private const val PREF_BLUR = "miuix_blur"

    var uiMode by mutableStateOf(UiMode.DEFAULT)
        private set
    var enableBlur by mutableStateOf(false)
        private set
    var floatingBar by mutableStateOf(false)
        private set
    var floatingBarBlur by mutableStateOf(false)
        private set

    private fun prefs(context: Context) =
        (context.applicationContext as KernelSUApplication).ensurePreferencesRepository()

    fun load(context: Context) {
        val p = prefs(context)
        uiMode = UiMode.fromValue(p.getString(UiMode.PREF_KEY, UiMode.DEFAULT.value))
        enableBlur = p.getBoolean(PREF_BLUR, false)
        floatingBar = p.getBoolean(PREF_FLOATING_BAR, false)
        floatingBarBlur = p.getBoolean(PREF_FLOATING_BAR_BLUR, false)
    }

    fun setUiMode(context: Context, mode: UiMode) {
        uiMode = mode
        prefs(context).putString(UiMode.PREF_KEY, mode.value)
    }

    fun setBlur(context: Context, v: Boolean) {
        enableBlur = v
        prefs(context).putBoolean(PREF_BLUR, v)
    }

    fun setFloatingBar(context: Context, v: Boolean) {
        floatingBar = v
        prefs(context).putBoolean(PREF_FLOATING_BAR, v)
    }

    fun setFloatingBarBlur(context: Context, v: Boolean) {
        floatingBarBlur = v
        prefs(context).putBoolean(PREF_FLOATING_BAR_BLUR, v)
    }
}
