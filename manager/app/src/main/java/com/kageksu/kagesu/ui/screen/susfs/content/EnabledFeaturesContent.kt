package com.kageksu.kagesu.ui.screen.susfs.content

import androidx.compose.runtime.Composable
import com.kageksu.kagesu.ui.LocalUiMode
import com.kageksu.kagesu.ui.UiMode
import com.kageksu.kagesu.ui.screen.susfs.content.miuix.EnabledFeaturesContentMiuix
import com.kageksu.kagesu.ui.screen.susfs.content.material.EnabledFeaturesContentMaterial
import com.kageksu.kagesu.ui.screen.susfs.util.SuSFSManager

@Composable
fun EnabledFeaturesContent(
    enabledFeatures: List<SuSFSManager.EnabledFeature>,
    onRefresh: () -> Unit
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> EnabledFeaturesContentMiuix(
            enabledFeatures = enabledFeatures,
            onRefresh = onRefresh
        )
        UiMode.Material -> EnabledFeaturesContentMaterial(
            enabledFeatures = enabledFeatures,
            onRefresh = onRefresh
        )
    }
}
