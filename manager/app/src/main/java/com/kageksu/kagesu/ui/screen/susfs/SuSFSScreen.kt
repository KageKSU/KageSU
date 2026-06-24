package com.kageksu.kagesu.ui.screen.susfs

import androidx.compose.runtime.Composable
import com.kageksu.kagesu.ui.LocalUiMode
import com.kageksu.kagesu.ui.UiMode

@Composable
fun SuSFSScreen() {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SuSFSMiuix()
        UiMode.Material -> SuSFSMaterial()
    }
}
