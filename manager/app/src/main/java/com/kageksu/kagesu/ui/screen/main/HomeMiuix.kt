// Miuix UI originally authored by YuKongA (https://github.com/YuKongA) for SukiSU-Ultra
// (https://github.com/SukiSU-Ultra/SukiSU-Ultra), GPL-3.0. Original author retains
// copyright; adapted for KageSU on the ReSukiSU base. See docs/ATTRIBUTION.md.

package com.kageksu.kagesu.ui.screen.main

import android.content.Intent
import android.system.Os
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kageksu.kagesu.BuildConfig
import com.kageksu.kagesu.Natives
import com.kageksu.kagesu.R
import com.kageksu.kagesu.ksuApp
import com.kageksu.kagesu.magica.MagicaService
import com.kageksu.kagesu.ui.component.ksuIsValid
import com.kageksu.kagesu.ui.component.miuix.WarningCard
import com.kageksu.kagesu.ui.component.rebootlistpopup.RebootListPopupMiuix
import com.kageksu.kagesu.ui.component.rememberConfirmDialog
import com.kageksu.kagesu.ui.component.rememberLoadingDialog
import com.kageksu.kagesu.ui.navigation.LocalNavigator
import com.kageksu.kagesu.ui.navigation.Route
import com.kageksu.kagesu.ui.theme.LocalEnableBlur
import com.kageksu.kagesu.ui.theme.isInDarkTheme
import com.kageksu.kagesu.ui.util.BlurredBar
import com.kageksu.kagesu.ui.util.LocalHandlePageChange
import com.kageksu.kagesu.ui.util.module.LatestVersionInfo
import com.kageksu.kagesu.ui.util.rememberBlurBackdrop
import com.kageksu.kagesu.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.isDynamicColor
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * Miuix rendering of the Home page, ported from SukiSU-Ultra (GPL-3.0) and wired to
 * ReSukiSU's [HomeViewModel]. Provides the collapsing top bar + scroll-end haptics.
 */
@Composable
fun HomeMiuix(bottomPadding: Dp) {
    val context = LocalContext.current
    val viewModel = viewModel<HomeViewModel>(viewModelStoreOwner = ksuApp)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.awaitInitialData(context)
    }

    if (!uiState.isInitialDataLoaded) return

    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else colorScheme.surface

    val navigator = LocalNavigator.current
    val handlePageChange = LocalHandlePageChange.current
    val loadingDialog = rememberLoadingDialog()
    val scope = rememberCoroutineScope()

    val status = uiState.systemStatus
    val info = uiState.systemInfo

    val onJailbreak: () -> Unit = {
        loadingDialog.showLoading()
        context.startService(Intent(context, MagicaService::class.java))
        scope.launch(Dispatchers.IO) {
            delay(30_000)
            withContext(Dispatchers.Main) {
                loadingDialog.hide()
                Toast.makeText(context, R.string.jailbreak_timeout, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
                backdrop = backdrop,
                barColor = barColor,
                showSusfs = info.susfsVersionSupported,
                onSusfs = { navigator.push(Route.SuSFSConfig) },
            )
        },

        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(horizontal = 12.dp),
                contentPadding = innerPadding,
                overscrollEffect = null,
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (uiState.isCoreDataLoaded) {
                            UpdateCard(uiState.latestVersionInfo)

                            if (status.requireNewKernel) {
                                if ((status.ksuVersion ?: 0) > BuildConfig.VERSION_CODE) {
                                    WarningCard(
                                        stringResource(
                                            R.string.require_manager_version,
                                            BuildConfig.VERSION_CODE,
                                            status.ksuVersion ?: 0
                                        )
                                    )
                                } else {
                                    WarningCard(
                                        stringResource(
                                            R.string.require_kernel_version,
                                            status.ksuVersion ?: 0,
                                            BuildConfig.VERSION_CODE
                                        )
                                    )
                                }
                            }

                            if (BuildConfig.DEBUG) {
                                WarningCard(stringResource(R.string.debug_version_notice))
                            }

                            if (!status.isOfficialSignature) {
                                WarningCard(
                                    stringResource(
                                        R.string.unofficial_version_notice,
                                        stringResource(R.string.app_name)
                                    )
                                )
                            }

                            if (BuildConfig.IS_PR_BUILD || Natives.isPrBuild) {
                                WarningCard(stringResource(R.string.home_pr_build_warning))
                            }

                            if (status.kernelPatchImplement == Natives.KernelPatchImplement.KERNEL_PATCH_OFFICIAL) {
                                WarningCard(stringResource(R.string.conflict_with_apatch))
                            }

                            if (status.ksuVersion != null && !status.isRootAvailable) {
                                WarningCard(stringResource(R.string.grant_root_failed))
                            }

                            StatusCard(
                                status = status,
                                superuserCount = info.superuserCount,
                                moduleCount = info.moduleCount,
                                onInstallClick = {
                                    navigator.push(Route.Install(preselectedKernelUri = null))
                                },
                                onSuperuserClick = { handlePageChange(1) },
                                onModuleClick = { handlePageChange(2) },
                                onJailbreakClick = onJailbreak,
                            )
                        }

                        if (uiState.isExtendedDataLoaded) {
                            InfoCard(
                                status = status,
                                info = info,
                                isSimpleMode = uiState.isSimpleMode,
                                isHideSusfsStatus = uiState.isHideSusfsStatus,
                                isHideZygiskImplement = uiState.isHideZygiskImplement,
                                isHideMetaModuleImplement = uiState.isHideMetaModuleImplement,
                            )
                        }

                        if (!uiState.isSimpleMode && !uiState.isHideLinkCard) {
                            DonateCard()
                            LearnMoreCard()
                        }

                        Spacer(Modifier.height(bottomPadding))
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateCard(newVersion: LatestVersionInfo) {
    val uriHandler = LocalUriHandler.current
    val title = stringResource(id = R.string.module_changelog)
    val updateText = stringResource(id = R.string.module_update)
    val updateDialog = rememberConfirmDialog(onConfirm = { uriHandler.openUri(newVersion.downloadUrl) })
    val hasUpdate = newVersion.versionCode > BuildConfig.VERSION_CODE

    AnimatedVisibility(
        visible = hasUpdate,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut()
    ) {
        WarningCard(
            message = stringResource(id = R.string.new_version_available).format(newVersion.versionCode),
            color = colorScheme.outline,
            onClick = {
                if (newVersion.changelog.isEmpty()) {
                    uriHandler.openUri(newVersion.downloadUrl)
                } else {
                    updateDialog.showConfirm(
                        title = title,
                        content = newVersion.changelog,
                        markdown = true,
                        confirm = updateText
                    )
                }
            }
        )
    }
}

@Composable
private fun TopBar(
    scrollBehavior: ScrollBehavior,
    backdrop: LayerBackdrop?,
    barColor: Color,
    showSusfs: Boolean,
    onSusfs: () -> Unit,
) {
    BlurredBar(backdrop) {
        TopAppBar(
            color = barColor,
            title = stringResource(R.string.app_name),
            actions = {
                if (showSusfs) {
                    top.yukonga.miuix.kmp.basic.IconButton(onClick = onSusfs) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = stringResource(R.string.susfs_config_setting_title),
                            tint = colorScheme.onBackground,
                        )
                    }
                }
                RebootListPopupMiuix()
            },
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
private fun StatusCard(
    status: HomeViewModel.SystemStatus,
    superuserCount: Int,
    moduleCount: Int,
    onInstallClick: () -> Unit,
    onSuperuserClick: () -> Unit,
    onModuleClick: () -> Unit,
    onJailbreakClick: () -> Unit,
) {
    Column {
        when {
            status.ksuVersion != null -> {
                val workingState = buildString {
                    if (Natives.isSafeMode) append(" [${stringResource(id = R.string.safe_mode)}]")
                    if (Natives.isLateLoadMode) append(" [${stringResource(id = R.string.jailbreak_mode)}]")
                }
                val workingMode = when (status.lkmMode) {
                    null -> ""
                    true -> " <LKM>"
                    else -> " <Built-in>"
                }
                val workingText = "${stringResource(id = R.string.home_working)}$workingMode$workingState"
                val versionText = status.ksuFullVersion ?: "${status.ksuVersion}-${status.kernelUAPIVersion}"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = CardDefaults.defaultColors(
                            color = when {
                                isDynamicColor -> colorScheme.secondaryContainer
                                isInDarkTheme() -> Color(0xFF1A3825)
                                else -> Color(0xFFDFFAE4)
                            }
                        ),
                        onClick = {
                            if (!Natives.isLateLoadMode) onInstallClick()
                        },
                        showIndication = !Natives.isLateLoadMode,
                        pressFeedbackType = PressFeedbackType.Tilt
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(38.dp, 45.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Icon(
                                    modifier = Modifier.size(170.dp),
                                    imageVector = Icons.Rounded.CheckCircleOutline,
                                    tint = if (isDynamicColor) colorScheme.primary.copy(alpha = 0.8f)
                                    else Color(0xFF36D167),
                                    contentDescription = null
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(all = 16.dp)
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = workingText,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.home_working_version, versionText),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        CountCard(
                            label = stringResource(R.string.superuser),
                            count = superuserCount,
                            onClick = onSuperuserClick,
                        )
                        Spacer(Modifier.height(12.dp))
                        CountCard(
                            label = stringResource(R.string.module),
                            count = moduleCount,
                            onClick = onModuleClick,
                        )
                    }
                }
            }

            status.kernelVersion.isGKI() -> {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        onClick = { if (!Natives.isLateLoadMode) onInstallClick() },
                        showIndication = !Natives.isLateLoadMode,
                        pressFeedbackType = PressFeedbackType.Sink
                    ) {
                        BasicComponent(
                            title = stringResource(R.string.home_not_installed),
                            summary = stringResource(R.string.home_click_to_install),
                            startAction = {
                                Icon(
                                    Icons.Rounded.ErrorOutline,
                                    stringResource(R.string.home_not_installed),
                                    modifier = Modifier.padding(end = 6.dp),
                                    tint = colorScheme.onBackground,
                                )
                            },
                            endActions = {
                                if (status.isSELinuxPermissive) {
                                    TextButton(
                                        text = stringResource(R.string.home_jailbreak),
                                        onClick = onJailbreakClick,
                                        colors = ButtonDefaults.textButtonColorsPrimary()
                                    )
                                }
                            }
                        )
                    }
                }
            }

            else -> {
                Card(
                    onClick = { if (!Natives.isLateLoadMode) onInstallClick() },
                    showIndication = !Natives.isLateLoadMode,
                    pressFeedbackType = PressFeedbackType.Sink
                ) {
                    BasicComponent(
                        title = stringResource(R.string.home_unsupported),
                        summary = stringResource(R.string.home_unsupported_reason),
                        startAction = {
                            Icon(
                                Icons.Rounded.ErrorOutline,
                                stringResource(R.string.home_unsupported),
                                modifier = Modifier.padding(end = 16.dp),
                                tint = colorScheme.onBackground,
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.CountCard(
    label: String,
    count: Int,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        insideMargin = PaddingValues(16.dp),
        onClick = onClick,
        showIndication = true,
        pressFeedbackType = PressFeedbackType.Tilt
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = label,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = colorScheme.onSurfaceVariantSummary,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = count.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun LearnMoreCard() {
    val uriHandler = LocalUriHandler.current
    val url = stringResource(R.string.home_learn_kernelsu_url)
    Card(modifier = Modifier.fillMaxWidth()) {
        BasicComponent(
            title = stringResource(R.string.home_learn_kernelsu),
            summary = stringResource(R.string.home_click_to_learn_kernelsu),
            endActions = {
                Icon(
                    imageVector = Icons.Filled.Link,
                    tint = colorScheme.onSurface,
                    contentDescription = null
                )
            },
            onClick = { uriHandler.openUri(url) }
        )
    }
}

@Composable
private fun DonateCard() {
    val uriHandler = LocalUriHandler.current
    Card(modifier = Modifier.fillMaxWidth()) {
        BasicComponent(
            title = stringResource(R.string.home_support_title),
            summary = stringResource(R.string.home_support_content),
            endActions = {
                Icon(
                    imageVector = Icons.Filled.Link,
                    tint = colorScheme.onSurface,
                    contentDescription = null
                )
            },
            onClick = { uriHandler.openUri("https://patreon.com/weishu") },
            insideMargin = PaddingValues(18.dp)
        )
    }
}

@Composable
private fun InfoCard(
    status: HomeViewModel.SystemStatus,
    info: HomeViewModel.SystemInfo,
    isSimpleMode: Boolean,
    isHideSusfsStatus: Boolean,
    isHideZygiskImplement: Boolean,
    isHideMetaModuleImplement: Boolean,
) {
    @Composable
    fun InfoText(title: String, content: String, bottomPadding: Dp = 24.dp) {
        Text(
            text = title,
            fontSize = MiuixTheme.textStyles.headline1.fontSize,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurface
        )
        Text(
            text = content,
            fontSize = MiuixTheme.textStyles.body2.fontSize,
            color = colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(top = 2.dp, bottom = bottomPadding)
        )
    }

    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            InfoText(stringResource(R.string.home_kernel), info.kernelRelease)

            if (!isSimpleMode) {
                InfoText(stringResource(R.string.home_android_version), info.androidVersion)
            }

            InfoText(stringResource(R.string.home_device_model), info.deviceModel)

            InfoText(
                stringResource(R.string.home_manager_version),
                "${info.managerVersion.first} (${info.managerVersion.second}/${info.managerVersion.third})"
            )

            if (!isSimpleMode && ksuIsValid()) {
                InfoText(stringResource(R.string.home_hook_type), Natives.getHookType())
            }

            if (!isSimpleMode && info.managersList != null) {
                val signatureMap = info.managersList.managers.groupBy { it.signatureIndex }
                val managersText = buildString {
                    signatureMap.toSortedMap().forEach { (signatureIndex, managers) ->
                        append(managers.joinToString(", ") { "UID: ${it.uid}" })
                        append(" ")
                        append(
                            when (signatureIndex) {
                                0 -> "(${stringResource(R.string.app_name)})"
                                255 -> "(${stringResource(R.string.dynamic_managerature)})"
                                else -> if (signatureIndex >= 1) "(${
                                    stringResource(R.string.signature_index, signatureIndex)
                                })" else "(${stringResource(R.string.unknown_signature)})"
                            }
                        )
                        append(" | ")
                    }
                }.trimEnd(' ', '|')

                InfoText(
                    stringResource(R.string.multi_manager_list),
                    managersText.ifEmpty { stringResource(R.string.no_active_manager) }
                )
            }

            InfoText(stringResource(R.string.home_selinux_status), info.selinuxStatus)

            val seccompDisplay = when (info.seccompStatus) {
                -1 -> stringResource(R.string.seccomp_status_not_supported)
                0 -> stringResource(R.string.seccomp_status_disabled)
                1 -> stringResource(R.string.seccomp_status_strict)
                2 -> stringResource(R.string.seccomp_status_filter)
                else -> stringResource(R.string.seccomp_status_unknown)
            }

            val showZygisk =
                !isHideZygiskImplement && !isSimpleMode && info.zygiskImplement.isNotEmpty() && info.zygiskImplement != "None"
            val showMeta =
                !isHideMetaModuleImplement && !isSimpleMode && info.metaModuleImplement.isNotEmpty() && info.metaModuleImplement != "None"
            val showSusfs =
                !isSimpleMode && !isHideSusfsStatus && info.susfsEnabled && info.susfsVersion.isNotEmpty()

            InfoText(
                stringResource(R.string.home_seccomp_status),
                seccompDisplay,
                bottomPadding = if (showZygisk || showMeta || showSusfs) 24.dp else 0.dp
            )

            if (showZygisk) {
                InfoText(
                    stringResource(R.string.home_zygisk_implement),
                    info.zygiskImplement,
                    bottomPadding = if (showMeta || showSusfs) 24.dp else 0.dp
                )
            }
            if (showMeta) {
                InfoText(
                    stringResource(R.string.home_meta_module_implement),
                    info.metaModuleImplement,
                    bottomPadding = if (showSusfs) 24.dp else 0.dp
                )
            }
            if (showSusfs) {
                InfoText(stringResource(R.string.home_susfs_version), info.susfsVersion, bottomPadding = 0.dp)
            }
        }
    }
}
