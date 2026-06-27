package com.kageksu.kagesu.ui.screen.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.dropUnlessResumed
import com.kageksu.kagesu.BuildConfig
import com.kageksu.kagesu.R
import com.kageksu.kagesu.ui.LocalUiMode
import com.kageksu.kagesu.ui.UiMode
import com.kageksu.kagesu.ui.navigation3.LocalNavigator

@Composable
fun AboutScreen() {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    val htmlString = stringResource(
        id = R.string.about_source_code,
        "<b><a href=\"https://github.com/KageKSU/KageSU\">GitHub</a></b>",
        "<b><a href=\"https://t.me/KageKSU\">Telegram</a></b>",
        "<b><a href=\"https://kagesu.palaz.uk\">kagesu.palaz.uk</a></b>"
    )
    val state = AboutUiState(
        title = stringResource(R.string.about),
        appName = stringResource(R.string.app_name),
        versionName = BuildConfig.VERSION_NAME,
        links = extractLinks(htmlString),
    )
    val actions = AboutScreenActions(
        onBack = dropUnlessResumed { navigator.pop() },
        onOpenLink = uriHandler::openUri,
    )

    when (LocalUiMode.current) {
        UiMode.Miuix -> AboutScreenMiuix(state, actions)
        UiMode.Material -> AboutScreenMaterial(state, actions)
    }
}
