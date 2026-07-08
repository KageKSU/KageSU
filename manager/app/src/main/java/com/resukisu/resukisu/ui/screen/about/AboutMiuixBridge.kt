// Miuix UI ported from tiann/KernelSU (github.com/tiann/KernelSU), originally
// authored by YuKongA (github.com/YuKongA). This bridge feeds tiann's Miuix About
// screen from KageSU's resources/navigation. GPL-3.0. See docs/ATTRIBUTION.md.
package com.resukisu.resukisu.ui.screen.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.resukisu.resukisu.BuildConfig
import com.resukisu.resukisu.R
import com.resukisu.resukisu.ui.navigation.LocalNavigator

/**
 * Renders tiann's Miuix About screen, wired to KageSU's navigation and links.
 * Called from [AboutScreen] when [com.resukisu.resukisu.ui.UiMode.Miuix] is active.
 */
@Composable
fun AboutMiuixContent() {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current

    val sourceLabel = stringResource(R.string.get_source_code)
    val telegramLabel = stringResource(R.string.join_telegram_group)
    val links = extractLinks(
        "<a href=\"https://github.com/KageKSU/KageSU\">$sourceLabel</a><br/>" +
            "<a href=\"https://t.me/KageKSU\">$telegramLabel</a>"
    )

    AboutScreenMiuix(
        state = AboutUiState(
            title = stringResource(R.string.about),
            appName = stringResource(R.string.app_name),
            versionName = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            links = links,
        ),
        actions = AboutScreenActions(
            onBack = { navigator.pop() },
            onOpenLink = { url -> uriHandler.openUri(url) },
        ),
    )
}
