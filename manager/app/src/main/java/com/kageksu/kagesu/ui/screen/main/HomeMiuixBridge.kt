// Miuix UI ported from tiann/KernelSU (github.com/tiann/KernelSU), originally
// authored by YuKongA (github.com/YuKongA). This bridge adapts tiann's Miuix Home
// screen onto KageSU/ReSukiSU's HomeViewModel. GPL-3.0. See docs/ATTRIBUTION.md.
package com.kageksu.kagesu.ui.screen.main

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import com.kageksu.kagesu.BuildConfig
import com.kageksu.kagesu.ui.navigation.LocalNavigator
import com.kageksu.kagesu.ui.navigation.Route
import com.kageksu.kagesu.ui.screen.home.HomeActions
import com.kageksu.kagesu.ui.screen.home.HomePagerMiuix
import com.kageksu.kagesu.ui.util.LocalHandlePageChange
import com.kageksu.kagesu.ui.viewmodel.HomeUiState as OurHomeUiState
import com.kageksu.kagesu.ui.screen.home.HomeUiState as MiuixHomeUiState
import com.kageksu.kagesu.ui.screen.home.SystemInfo as MiuixSystemInfo

/**
 * Renders tiann's Miuix Home pager, fed by our HomeViewModel state and wired to our
 * navigation. Called from [HomePage] when [com.kageksu.kagesu.ui.UiMode.Miuix] is active.
 */
@Composable
fun HomeMiuixContent(uiState: OurHomeUiState, bottomPadding: Dp) {
    val navigator = LocalNavigator.current
    val handlePageChange = LocalHandlePageChange.current
    val uriHandler = LocalUriHandler.current

    HomePagerMiuix(
        state = uiState.toMiuixHomeUiState(),
        actions = HomeActions(
            onInstallClick = { navigator.push(Route.Install(preselectedKernelUri = null)) },
            onSuperuserClick = { handlePageChange(1) },
            onModuleClick = { handlePageChange(2) },
            onOpenUrl = { url -> uriHandler.openUri(url) },
        ),
        bottomInnerPadding = bottomPadding,
    )
}

/** Maps our nested HomeViewModel state to tiann's flat Miuix HomeUiState. */
private fun OurHomeUiState.toMiuixHomeUiState(): MiuixHomeUiState {
    val status = systemStatus
    val info = systemInfo
    return MiuixHomeUiState(
        kernelVersion = status.kernelVersion,
        ksuVersion = status.ksuVersion,
        managerUAPIVersion = status.managerUAPIVersion,
        kernelUAPIVersion = status.kernelUAPIVersion,
        lkmMode = status.lkmMode,
        isManager = status.isManager,
        // PR-build / safe-mode / late-load are not tracked by our viewmodel.
        isManagerPrBuild = false,
        isKernelPrBuild = false,
        requiresNewKernel = status.requireNewKernel,
        uapiMismatch = status.uapiMismatch,
        isRootAvailable = status.isRootAvailable,
        isSafeMode = false,
        isLateLoadMode = false,
        checkUpdateEnabled = true,
        latestVersionInfo = latestVersionInfo,
        currentManagerVersionCode = BuildConfig.VERSION_CODE.toLong(),
        superuserCount = info.superuserCount,
        moduleCount = info.moduleCount,
        systemInfo = MiuixSystemInfo(
            kernelVersion = info.kernelRelease,
            managerVersion = info.managerVersion.first,
            deviceModel = info.deviceModel,
            fingerprint = Build.FINGERPRINT,
            selinuxStatus = info.selinuxStatus,
            seccompStatus = info.seccompStatus,
        ),
    )
}
