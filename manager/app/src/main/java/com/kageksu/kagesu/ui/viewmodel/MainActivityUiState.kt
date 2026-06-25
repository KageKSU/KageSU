package com.kageksu.kagesu.ui.viewmodel

import androidx.compose.runtime.Immutable
import com.kageksu.kagesu.ui.UiMode
import com.kageksu.kagesu.ui.theme.AppSettings

@Immutable
data class MainActivityUiState(
    val appSettings: AppSettings,
    val pageScale: Float,
    val enableBlur: Boolean,
    val enableFloatingBottomBar: Boolean,
    val enableFloatingBottomBarBlur: Boolean,
    val uiMode: UiMode,
    val backgroundEnabled: Boolean = false,
    val backgroundPath: String = "",
    val backgroundDim: Float = 0.4f,
    val backgroundBlurEnabled: Boolean = false,
    val backgroundBlurRadius: Float = 16f,
)
