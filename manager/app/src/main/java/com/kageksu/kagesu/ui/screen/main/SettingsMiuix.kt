package com.kageksu.kagesu.ui.screen.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fence
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.FolderDelete
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material.icons.rounded.RemoveModerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kageksu.kagesu.BuildConfig
import com.kageksu.kagesu.Natives
import com.kageksu.kagesu.R
import com.kageksu.kagesu.ksuApp
import com.kageksu.kagesu.ui.component.KsuIsValid
import com.kageksu.kagesu.ui.component.ksuIsValid
import com.kageksu.kagesu.ui.component.rememberLoadingDialog
import com.kageksu.kagesu.ui.navigation.LocalNavigator
import com.kageksu.kagesu.ui.navigation.Route
import com.kageksu.kagesu.ui.theme.LocalEnableBlur
import com.kageksu.kagesu.ui.util.BlurredBar
import com.kageksu.kagesu.ui.util.getBugreportFile
import com.kageksu.kagesu.ui.util.rememberBlurBackdrop
import com.kageksu.kagesu.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Miuix rendering of the Settings page. Miuix preference widgets (SukiSU-Ultra
 * GPL-3.0 design) wired to ReSukiSU's SettingsViewModel, preserving ReSukiSU's
 * full option set (sucompat modes, kernel umount, auto-jailbreak, adb root,
 * sulog, selinux hide, umount modules, send log, dynamic manager, umount path
 * manager, uninstall, theme settings, about).
 */
@Composable
fun SettingsMiuix(bottomPadding: Dp) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val viewModel = viewModel<SettingsViewModel>(viewModelStoreOwner = ksuApp)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val loadingDialog = rememberLoadingDialog()

    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val barColor = if (backdrop != null) Color.Transparent else colorScheme.surface

    val exportBugreportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/gzip")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            loadingDialog.show()
            context.contentResolver.openOutputStream(uri)?.use { output ->
                getBugreportFile(context).inputStream().use { it.copyTo(output) }
            }
            loadingDialog.hide()
        }
    }
    val sendLogLabel = stringResource(R.string.send_log)

    LaunchedEffect(Unit) { viewModel.loadFeatureSettings(context) }

    val iconMod = Modifier.padding(end = 6.dp)

    Scaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.settings),
                    scrollBehavior = scrollBehavior,
                )
            }
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
                if (ksuIsValid()) {
                    item {
                        SmallTitle(stringResource(R.string.configuration))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            ArrowPreference(
                                title = stringResource(R.string.settings_profile_template),
                                summary = stringResource(R.string.settings_profile_template_summary),
                                startAction = { Icon(Icons.Filled.Fence, null, iconMod, tint = colorScheme.onBackground) },
                                onClick = { navigator.push(Route.AppProfileTemplate) },
                            )
                            val modeItems = listOf(
                                stringResource(R.string.settings_mode_default),
                                stringResource(R.string.settings_mode_disable_until_reboot),
                                stringResource(R.string.settings_mode_disable_always),
                            )
                            OverlayDropdownPreference(
                                title = stringResource(R.string.settings_sucompat),
                                summary = featureSummary(uiState.suStatus, R.string.settings_sucompat_summary),
                                items = modeItems,
                                selectedIndex = uiState.suCompatMode,
                                enabled = uiState.suStatus == "supported",
                                startAction = { Icon(Icons.Rounded.RemoveModerator, null, iconMod, tint = colorScheme.onBackground) },
                                onSelectedIndexChange = { viewModel.handleSuCompatModeChange(context, it) },
                            )
                            SwitchPreference(
                                title = stringResource(R.string.settings_kernel_umount),
                                summary = featureSummary(uiState.kernelUmountStatus, R.string.settings_kernel_umount_summary),
                                checked = uiState.isKernelUmountEnabled,
                                enabled = uiState.kernelUmountStatus == "supported",
                                startAction = { Icon(Icons.Rounded.RemoveCircle, null, iconMod, tint = colorScheme.onBackground) },
                                onCheckedChange = viewModel::handleKernelUmountChange,
                            )
                            if (Natives.isLateLoadMode) {
                                SwitchPreference(
                                    title = stringResource(R.string.settings_auto_jailbreak),
                                    summary = stringResource(R.string.settings_auto_jailbreak_summary),
                                    checked = uiState.autoJailbreakEnabled,
                                    startAction = { Icon(Icons.Rounded.ElectricalServices, null, iconMod, tint = colorScheme.onBackground) },
                                    onCheckedChange = { viewModel.handleAutoJailbreakChange(context, it) },
                                )
                            }
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                                SwitchPreference(
                                    title = stringResource(R.string.settings_adb_root),
                                    summary = featureSummary(uiState.adbRootStatus, R.string.settings_adb_root_summary),
                                    checked = uiState.isAdbRootEnabled,
                                    enabled = uiState.adbRootStatus == "supported",
                                    startAction = { Icon(Icons.Filled.Adb, null, iconMod, tint = colorScheme.onBackground) },
                                    onCheckedChange = viewModel::handleAdbRootChange,
                                )
                            }
                            SwitchPreference(
                                title = stringResource(R.string.settings_sulog),
                                summary = featureSummary(uiState.sulogStatus, R.string.settings_sulog_summary),
                                checked = uiState.isSuLogEnabled,
                                enabled = uiState.sulogStatus == "supported",
                                startAction = { Icon(Icons.AutoMirrored.Rounded.Article, null, iconMod, tint = colorScheme.onBackground) },
                                onCheckedChange = viewModel::handleSuLogChange,
                            )
                            SwitchPreference(
                                title = stringResource(R.string.settings_selinux_hide),
                                summary = featureSummary(uiState.selinuxHideStatus, R.string.settings_selinux_hide_summary),
                                checked = uiState.isSelinuxHideEnabled,
                                enabled = uiState.selinuxHideStatus == "supported",
                                startAction = { Icon(Icons.Filled.Policy, null, iconMod, tint = colorScheme.onBackground) },
                                onCheckedChange = { viewModel.handleSelinuxHideChange(context, it) },
                            )
                            SwitchPreference(
                                title = stringResource(R.string.settings_umount_modules_default),
                                summary = stringResource(R.string.settings_umount_modules_default_summary),
                                checked = uiState.defaultUmountModules,
                                startAction = { Icon(Icons.Rounded.FolderDelete, null, iconMod, tint = colorScheme.onBackground) },
                                onCheckedChange = viewModel::handleDefaultUmountModulesChange,
                            )
                        }
                    }
                }

                item {
                    SmallTitle(stringResource(R.string.app_settings))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        SwitchPreference(
                            title = stringResource(R.string.settings_check_update),
                            summary = stringResource(R.string.settings_check_update_summary),
                            checked = uiState.checkUpdate,
                            startAction = { Icon(Icons.Filled.Update, null, iconMod, tint = colorScheme.onBackground) },
                            onCheckedChange = { viewModel.handleCheckUpdateChange(context, it) },
                        )
                        ArrowPreference(
                            title = stringResource(R.string.theme_settings),
                            summary = stringResource(R.string.theme_settings),
                            startAction = { Icon(Icons.Filled.Palette, null, iconMod, tint = colorScheme.onBackground) },
                            onClick = { navigator.push(Route.ThemeSettings) },
                        )
                    }
                }

                item {
                    SmallTitle(stringResource(R.string.tools))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        ArrowPreference(
                            title = stringResource(R.string.save_log),
                            startAction = { Icon(Icons.Filled.Save, null, iconMod, tint = colorScheme.onBackground) },
                            onClick = {
                                val current = LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm"))
                                exportBugreportLauncher.launch("KernelSU_bugreport_${current}.tar.gz")
                            },
                        )
                        ArrowPreference(
                            title = stringResource(R.string.send_log),
                            startAction = { Icon(Icons.Filled.Share, null, iconMod, tint = colorScheme.onBackground) },
                            onClick = {
                                scope.launch {
                                    val bugreport = loadingDialog.withLoading {
                                        withContext(Dispatchers.IO) { getBugreportFile(context) }
                                    }
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                                        bugreport
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        setDataAndType(uri, "application/gzip")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, sendLogLabel))
                                }
                            },
                        )
                        if (ksuIsValid()) {
                            ArrowPreference(
                                title = stringResource(R.string.dynamic_manager_title),
                                summary = stringResource(R.string.dynamic_manager_settings_summary),
                                startAction = { Icon(Icons.Filled.Security, null, iconMod, tint = colorScheme.onBackground) },
                                onClick = { navigator.push(Route.DynamicManager) },
                            )
                            if (uiState.isKernelUmountEnabled) {
                                ArrowPreference(
                                    title = stringResource(R.string.umount_path_manager),
                                    summary = stringResource(R.string.umount_path_manager_summary),
                                    startAction = { Icon(Icons.Filled.FolderOff, null, iconMod, tint = colorScheme.onBackground) },
                                    onClick = { navigator.push(Route.UmountManager) },
                                )
                            }
                        }
                        if (Natives.isLkmMode) {
                            UninstallArrowPreference(iconMod) { loadingDialog.withLoading(it) }
                        }
                    }
                }

                item {
                    SmallTitle(stringResource(R.string.about))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        ArrowPreference(
                            title = stringResource(R.string.about),
                            startAction = { Icon(Icons.Filled.Info, null, iconMod, tint = colorScheme.onBackground) },
                            onClick = { navigator.push(Route.About) },
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(bottomPadding + innerPadding.calculateBottomPadding() + 12.dp))
                }
            }
        }
    }
}

@Composable
private fun featureSummary(status: String, defaultRes: Int): String = when (status) {
    "unsupported" -> stringResource(R.string.feature_status_unsupported_summary)
    "managed" -> stringResource(R.string.feature_status_managed_summary)
    else -> stringResource(defaultRes)
}

@Composable
private fun UninstallArrowPreference(
    iconMod: Modifier,
    withLoading: suspend (suspend () -> Unit) -> Unit,
) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uninstallConfirmDialog = com.kageksu.kagesu.ui.component.rememberConfirmDialog()
    val uninstallDialog = rememberUninstallDialog { uninstallType ->
        scope.launch {
            val result = uninstallConfirmDialog.awaitConfirm(
                title = context.getString(uninstallType.title),
                content = context.getString(uninstallType.message)
            )
            if (result == com.kageksu.kagesu.ui.component.ConfirmResult.Confirmed) {
                withLoading {
                    when (uninstallType) {
                        UninstallType.TEMPORARY -> Unit
                        UninstallType.PERMANENT -> navigator.push(Route.Flash(com.kageksu.kagesu.ui.screen.FlashIt.FlashUninstall))
                        UninstallType.RESTORE_STOCK_IMAGE -> navigator.push(Route.Flash(com.kageksu.kagesu.ui.screen.FlashIt.FlashRestore))
                        UninstallType.NONE -> Unit
                    }
                }
            }
        }
    }
    ArrowPreference(
        title = stringResource(R.string.settings_uninstall),
        startAction = { Icon(Icons.Filled.Delete, null, iconMod, tint = colorScheme.onBackground) },
        onClick = { uninstallDialog.show() },
    )
}
