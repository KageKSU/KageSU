package com.kageksu.kagesu.ui.screen.susfs.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kageksu.kagesu.ui.LocalUiMode
import com.kageksu.kagesu.ui.UiMode
import com.kageksu.kagesu.ui.screen.susfs.component.miuix.FeatureStatusCardMiuix
import com.kageksu.kagesu.ui.screen.susfs.component.material.FeatureStatusCardMaterial
import com.kageksu.kagesu.ui.screen.susfs.util.SuSFSManager

@Composable
fun FeatureStatusCard(
    feature: SuSFSManager.EnabledFeature,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> FeatureStatusCardMiuix(
            feature = feature,
            onRefresh = onRefresh,
            modifier = modifier
        )
        UiMode.Material -> FeatureStatusCardMaterial(
            feature = feature,
            onRefresh = onRefresh,
            modifier = modifier
        )
    }
}
