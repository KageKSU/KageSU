// Miuix UI originally authored by YuKongA (https://github.com/YuKongA) for SukiSU-Ultra
// (https://github.com/SukiSU-Ultra/SukiSU-Ultra), GPL-3.0. Original author retains
// copyright; adapted for KageSU on the ReSukiSU base. See docs/ATTRIBUTION.md.

package com.kageksu.kagesu.ui.screen.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Wysiwyg
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kageksu.kagesu.Natives
import com.kageksu.kagesu.R
import com.kageksu.kagesu.data.appPreferences
import com.kageksu.kagesu.ksuApp
import com.kageksu.kagesu.ui.component.ConfirmResult
import com.kageksu.kagesu.ui.component.InstallConfirmationDialog
import com.kageksu.kagesu.ui.component.ZipFileDetector.parseModuleInfo
import com.kageksu.kagesu.ui.component.ZipFileInfo
import com.kageksu.kagesu.ui.component.ZipType
import com.kageksu.kagesu.ui.component.rememberConfirmDialog
import com.kageksu.kagesu.ui.component.rememberLoadingDialog
import com.kageksu.kagesu.ui.navigation.LocalNavigator
import com.kageksu.kagesu.ui.navigation.Route
import com.kageksu.kagesu.ui.screen.FlashIt
import com.kageksu.kagesu.ui.theme.LocalEnableBlur
import com.kageksu.kagesu.ui.theme.isInDarkTheme
import com.kageksu.kagesu.ui.util.BlurredBar
import com.kageksu.kagesu.ui.util.LocalPermissionRequestInterface
import com.kageksu.kagesu.ui.util.LocalSnackbarHost
import com.kageksu.kagesu.ui.util.downloader.download
import com.kageksu.kagesu.ui.util.hasMagisk
import com.kageksu.kagesu.ui.util.module.ModuleUtils
import com.kageksu.kagesu.ui.util.module.Shortcut
import com.kageksu.kagesu.ui.util.reboot
import com.kageksu.kagesu.ui.util.rememberBlurBackdrop
import com.kageksu.kagesu.ui.util.toggleModule
import com.kageksu.kagesu.ui.util.undoUninstallModule
import com.kageksu.kagesu.ui.util.uninstallModule
import com.kageksu.kagesu.ui.viewmodel.ModuleViewModel
import com.kageksu.kagesu.ui.webui.WebUIActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * Native Miuix Module page. Miuix chrome + list + cards + miuix PullToRefresh,
 * wired to ReSukiSU's ModuleViewModel and its module operations
 * (toggle/uninstall/update/webui/action). Dispatched from ModulePage when Miuix.
 */
@Composable
fun ModuleMiuix(bottomPadding: Dp) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val viewModel = viewModel<ModuleViewModel>(viewModelStoreOwner = ksuApp)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs = context.appPreferences
    val snackBarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val layoutDirection = LocalLayoutDirection.current
    val permissionRequestInterface = LocalPermissionRequestInterface.current

    val loadingDialog = rememberLoadingDialog()
    val confirmDialog = rememberConfirmDialog()

    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val barColor = if (backdrop != null) Color.Transparent else colorScheme.surface

    // Snackbar / dialog strings
    val rebootToApply = stringResource(R.string.reboot_to_apply)
    val rebootStr = stringResource(R.string.reboot)
    val failedEnable = stringResource(R.string.module_failed_to_enable)
    val failedDisable = stringResource(R.string.module_failed_to_disable)
    val successUninstall = stringResource(R.string.module_uninstall_success)
    val failedUninstall = stringResource(R.string.module_uninstall_failed)
    val moduleStr = stringResource(R.string.module)
    val uninstallStr = stringResource(R.string.uninstall)
    val cancelStr = stringResource(android.R.string.cancel)
    val moduleUninstallConfirm = stringResource(R.string.module_uninstall_confirm)
    val metaModuleUninstallConfirm = stringResource(R.string.metamodule_uninstall_confirm)
    val updateText = stringResource(R.string.module_update)
    val changelogText = stringResource(R.string.module_changelog)
    val downloadingText = stringResource(R.string.module_downloading)
    val startDownloadingText = stringResource(R.string.module_start_downloading)
    val fetchChangeLogFailed = stringResource(R.string.module_changelog_failed)

    // Install flow
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingZipFiles by remember { mutableStateOf<List<ZipFileInfo>>(emptyList()) }
    InstallConfirmationDialog(
        show = showConfirmationDialog,
        zipFiles = pendingZipFiles,
        onConfirm = { info ->
            showConfirmationDialog = false
            navigator.push(
                Route.Flash(FlashIt.FlashModules(ArrayList(info.filter { it.type == ZipType.MODULE }.map { it.uri })))
            )
            viewModel.markNeedRefresh()
        },
        onDismiss = {
            showConfirmationDialog = false
            pendingZipFiles = emptyList()
        }
    )
    val selectZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != RESULT_OK) return@rememberLauncherForActivityResult
        val data = it.data ?: return@rememberLauncherForActivityResult
        scope.launch {
            val zipFiles = mutableListOf<ZipFileInfo>()
            val clipData = data.clipData
            val uris = if (clipData != null) {
                (0 until clipData.itemCount).map { i -> clipData.getItemAt(i).uri }
            } else {
                listOfNotNull(data.data)
            }
            val accessible = uris.filter { uri ->
                try {
                    if (!ModuleUtils.isUriAccessible(context, uri)) return@filter false
                    ModuleUtils.takePersistableUriPermission(context, uri)
                    true
                } catch (e: Exception) {
                    Log.e("ModuleMiuix", "URI error: $uri, ${e.message}"); false
                }
            }
            if (accessible.isEmpty()) {
                snackBarHost.showSnackbar("Unable to access selected module files")
                return@launch
            }
            accessible.forEach { zipFiles.add(parseModuleInfo(context, it)) }
            pendingZipFiles = zipFiles
            showConfirmationDialog = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updateSearch("")
        viewModel.setSortOptions(
            sortEnabledFirst = prefs.getBoolean("module_sort_enabled_first", false),
            sortActionFirst = prefs.getBoolean("module_sort_action_first", false),
        )
        if (uiState.moduleList.isEmpty() || uiState.isNeedRefresh) {
            viewModel.fetchModuleList()
        }
    }

    val hasMagisk = hasMagisk()
    val hideInstallButton = Natives.isSafeMode || hasMagisk

    // Operations
    fun doToggle(module: ModuleViewModel.ModuleInfo) {
        viewModel.viewModelScope.launch {
            val ok = loadingDialog.withLoading {
                withContext(Dispatchers.IO) { toggleModule(module.dirId, !module.enabled) }
            }
            if (ok) {
                viewModel.fetchModuleList()
                val r = snackBarHost.showSnackbar(rebootToApply, rebootStr, duration = SnackbarDuration.Long)
                if (r == SnackbarResult.ActionPerformed) reboot()
            } else {
                snackBarHost.showSnackbar((if (module.enabled) failedDisable else failedEnable).format(module.name))
            }
        }
    }

    fun doUninstall(module: ModuleViewModel.ModuleInfo) {
        viewModel.viewModelScope.launch {
            val isUninstall = !module.remove
            if (isUninstall) {
                val fmt = if (module.metamodule) metaModuleUninstallConfirm else moduleUninstallConfirm
                val confirm = confirmDialog.awaitConfirm(
                    moduleStr, content = fmt.format(module.name), confirm = uninstallStr, dismiss = cancelStr
                )
                if (confirm != ConfirmResult.Confirmed) return@launch
            }
            val ok = loadingDialog.withLoading {
                withContext(Dispatchers.IO) {
                    if (isUninstall) {
                        Shortcut.deleteModuleActionShortcut(context, module.id)
                        Shortcut.deleteModuleWebUiShortcut(context, module.id)
                        uninstallModule(module.dirId)
                    } else undoUninstallModule(module.dirId)
                }
            }
            if (ok) {
                viewModel.fetchModuleList()
                viewModel.markNeedRefresh()
            }
            if (!isUninstall) return@launch
            val msg = (if (ok) successUninstall else failedUninstall).format(module.name)
            val r = snackBarHost.showSnackbar(msg, if (ok) rebootStr else null, duration = SnackbarDuration.Long)
            if (r == SnackbarResult.ActionPerformed) reboot()
        }
    }

    fun doUpdate(module: ModuleViewModel.ModuleInfo) {
        val upd = module.moduleUpdate ?: return
        viewModel.viewModelScope.launch {
            val req = okhttp3.Request.Builder().url(upd.changelog).build()
            val changelogResult = loadingDialog.withLoading {
                withContext(Dispatchers.IO) {
                    runCatching { ksuApp.okhttpClient.newCall(req).execute().body!!.string() }
                }
            }
            val changelog = changelogResult.getOrElse {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, fetchChangeLogFailed.format(it.message), Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            val confirm = confirmDialog.awaitConfirm(changelogText, content = changelog, markdown = true, confirm = updateText)
            if (confirm != ConfirmResult.Confirmed) return@launch
            withContext(Dispatchers.Main) {
                Toast.makeText(context, startDownloadingText.format(module.name), Toast.LENGTH_SHORT).show()
            }
            val fileName = "${module.name}-${upd.version}.zip"
            withContext(Dispatchers.IO) {
                download(
                    context, permissionRequestInterface, upd.zipUrl, fileName,
                    onDownloaded = { uri -> navigator.push(Route.Flash(FlashIt.FlashModuleUpdate(uri))) },
                    onDownloading = {
                        scope.launch(Dispatchers.Main) {
                            Toast.makeText(context, downloadingText.format(module.name), Toast.LENGTH_SHORT).show()
                        }
                    },
                )
            }
        }
    }

    var searchField by remember { mutableStateOf(TextFieldValue(uiState.search)) }
    LaunchedEffect(uiState.search) {
        if (uiState.search != searchField.text) {
            searchField = TextFieldValue(uiState.search, selection = TextRange(uiState.search.length))
        }
    }

    // Hide the install FAB on scroll down, reveal on scroll up (SukiSU-Ultra behavior).
    var fabVisible by remember { mutableStateOf(true) }
    var scrollDistance by remember { mutableFloatStateOf(0f) }
    val fabScrollConnection = remember(listState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val info = listState.layoutInfo
                val isScrolledToEnd = info.visibleItemsInfo.lastOrNull()?.index == info.totalItemsCount - 1 &&
                        (info.visibleItemsInfo.lastOrNull()?.size ?: 0) < info.viewportEndOffset
                if (!isScrolledToEnd) {
                    scrollDistance += available.y
                    if (scrollDistance < -50f) {
                        if (fabVisible) fabVisible = false
                        scrollDistance = 0f
                    } else if (scrollDistance > 50f) {
                        if (!fabVisible) fabVisible = true
                        scrollDistance = 0f
                    }
                }
                return Offset.Zero
            }
        }
    }
    val fabOffset by animateDpAsState(
        targetValue = if (fabVisible) 0.dp else 180.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        animationSpec = tween(durationMillis = 350),
        label = "moduleFabOffset",
    )

    Scaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.module),
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(Route.ModuleRepo) }) {
                            Icon(Icons.Outlined.Cloud, stringResource(R.string.module_repo), tint = colorScheme.onSurface)
                        }
                    },
                    actions = {
                        ModuleSortPopup(
                            sortActionFirst = uiState.sortActionFirst,
                            sortEnabledFirst = uiState.sortEnabledFirst,
                            onToggleActionFirst = {
                                val v = !uiState.sortActionFirst
                                viewModel.setSortActionFirst(v); prefs.putBoolean("module_sort_action_first", v)
                            },
                            onToggleEnabledFirst = {
                                val v = !uiState.sortEnabledFirst
                                viewModel.setSortEnabledFirst(v); prefs.putBoolean("module_sort_enabled_first", v)
                            },
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    bottomContent = {
                        TextField(
                            value = searchField,
                            onValueChange = { searchField = it; viewModel.updateSearch(it.text) },
                            label = stringResource(R.string.search_modules),
                            useLabelAsPlaceholder = true,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 6.dp),
                            leadingIcon = {
                                Icon(Icons.Filled.Search, null, Modifier.padding(start = 12.dp).size(20.dp), tint = colorScheme.onSurfaceContainerVariant)
                            },
                        )
                    },
                )
            }
        },
        floatingActionButton = {
            if (!hideInstallButton) {
                FloatingActionButton(
                    modifier = Modifier
                        .offset { IntOffset(x = 0, y = fabOffset.roundToPx()) }
                        .padding(bottom = bottomPadding + 20.dp, end = 20.dp),
                    onClick = {
                        selectZipLauncher.launch(
                            Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "application/zip"
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            }
                        )
                    },
                ) {
                    Icon(Icons.Filled.Add, stringResource(R.string.install), Modifier.size(32.dp), tint = colorScheme.onPrimary)
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        val pullState = rememberPullToRefreshState()
        val contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            start = innerPadding.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection),
            bottom = bottomPadding + 12.dp,
        )

        when {
            hasMagisk -> CenterState(Icons.Outlined.Warning, stringResource(R.string.module_magisk_conflict))
            uiState.moduleList.isEmpty() && uiState.isRefreshing ->
                Box(Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()), Alignment.Center) { InfiniteProgressIndicator() }
            uiState.moduleList.isEmpty() -> CenterState(Icons.Outlined.Extension, stringResource(R.string.module_empty))
            else -> PullToRefresh(
                isRefreshing = uiState.isRefreshing,
                pullToRefreshState = pullState,
                onRefresh = { viewModel.fetchModuleList(true) },
                contentPadding = contentPadding,
            ) {
                Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxHeight()
                            .scrollEndHaptic()
                            .overScrollVertical()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .nestedScroll(fabScrollConnection),
                        contentPadding = contentPadding,
                        overscrollEffect = null,
                    ) {
                        items(uiState.moduleList, key = { it.id }, contentType = { "module" }) { module ->
                                ModuleItemMiuix(
                                    module = module,
                                    updateUrl = module.moduleUpdate?.zipUrl.orEmpty(),
                                    onToggle = { doToggle(module) },
                                    onUninstall = { doUninstall(module) },
                                    onUpdate = { doUpdate(module) },
                                    onExecuteAction = {
                                        navigator.push(Route.ExecuteModuleAction(module.dirId))
                                        viewModel.markNeedRefresh()
                                    },
                                    onOpenWebUi = {
                                        if (module.hasWebUi) {
                                            try {
                                                context.startActivity(
                                                    Intent(context, WebUIActivity::class.java)
                                                        .setData("kernelsu://webui/${module.id}".toUri())
                                                        .putExtra("id", module.id)
                                                        .putExtra("name", module.name)
                                                )
                                            } catch (e: Exception) {
                                                scope.launch { snackBarHost.showSnackbar("Error launching WebUI: ${e.message}") }
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

    }
}

@Composable
private fun CenterState(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(96.dp).padding(bottom = 16.dp), tint = colorScheme.onSurfaceVariantSummary)
            Text(text = text, color = colorScheme.onSurfaceVariantSummary)
        }
    }
}

@Composable
private fun ModuleSortPopup(
    sortActionFirst: Boolean,
    sortEnabledFirst: Boolean,
    onToggleActionFirst: () -> Unit,
    onToggleEnabledFirst: () -> Unit,
) {
    Box {
        val show = remember { mutableStateOf(false) }
        OverlayListPopup(
            show = show.value,
            popupPositionProvider = ListPopupDefaults.DropdownPositionProvider,
            alignment = PopupPositionProvider.Align.TopEnd,
            onDismissRequest = { show.value = false },
        ) {
            ListPopupColumn {
                DropdownImpl(
                    text = stringResource(R.string.module_sort_action_first),
                    optionSize = 2, isSelected = sortActionFirst, index = 0,
                    onSelectedIndexChange = { onToggleActionFirst() },
                )
                DropdownImpl(
                    text = stringResource(R.string.module_sort_enabled_first),
                    optionSize = 2, isSelected = sortEnabledFirst, index = 1,
                    onSelectedIndexChange = { onToggleEnabledFirst() },
                )
            }
        }
        IconButton(onClick = { show.value = true }, holdDownState = show.value) {
            Icon(Icons.Filled.MoreVert, stringResource(R.string.settings), tint = colorScheme.onSurface)
        }
    }
}

@Composable
private fun ModuleItemMiuix(
    module: ModuleViewModel.ModuleInfo,
    updateUrl: String,
    onToggle: (Boolean) -> Unit,
    onUninstall: () -> Unit,
    onUpdate: () -> Unit,
    onExecuteAction: () -> Unit,
    onOpenWebUi: () -> Unit,
) {
    val secondaryContainer = colorScheme.secondaryContainer.copy(alpha = 0.8f)
    val actionIconTint = colorScheme.onSurface.copy(alpha = if (isInDarkTheme()) 0.7f else 0.9f)
    val updateBg = colorScheme.tertiaryContainer.copy(alpha = 0.6f)
    val updateTint = colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
    val hasUpdate = updateUrl.isNotEmpty()
    val decoration = if (module.remove) TextDecoration.LineThrough else null
    val hasDescription = module.description.isNotBlank()
    var expanded by rememberSaveable(module.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        insideMargin = PaddingValues(16.dp),
        onClick = { if (hasDescription) expanded = !expanded },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = module.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight(550),
                        color = colorScheme.onSurface,
                        textDecoration = decoration,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (module.metamodule) {
                        Text(
                            text = "META",
                            fontSize = 12.sp,
                            color = updateTint,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(updateBg).padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight(750),
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
                Text(
                    text = "${stringResource(R.string.module_version)}: ${module.version}",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp),
                    fontWeight = FontWeight(550),
                    color = colorScheme.onSurfaceVariantSummary,
                    textDecoration = decoration,
                )
                Text(
                    text = "${stringResource(R.string.module_author)}: ${module.author}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight(550),
                    color = colorScheme.onSurfaceVariantSummary,
                    textDecoration = decoration,
                )
            }
            Switch(
                enabled = !module.update,
                checked = module.enabled,
                onCheckedChange = { if (it != module.enabled) onToggle(it) },
            )
        }

        if (hasDescription) {
            Box(modifier = Modifier.padding(top = 2.dp).animateContentSize(tween(250, easing = FastOutSlowInEasing))) {
                Text(
                    text = module.description,
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                    overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    textDecoration = decoration,
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = colorScheme.outline.copy(alpha = 0.5f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(visible = module.enabled && !module.remove && !module.update, enter = fadeIn(), exit = fadeOut()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (module.hasActionScript) {
                        IconButton(minHeight = 35.dp, minWidth = 35.dp, backgroundColor = secondaryContainer, onClick = onExecuteAction) {
                            Icon(Icons.Outlined.PlayArrow, stringResource(R.string.action), Modifier.size(22.dp), tint = actionIconTint)
                        }
                    }
                    if (module.hasWebUi) {
                        IconButton(minHeight = 35.dp, minWidth = 35.dp, backgroundColor = secondaryContainer, onClick = onOpenWebUi) {
                            Icon(Icons.AutoMirrored.Outlined.Wysiwyg, null, Modifier.size(22.dp), tint = actionIconTint)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            if (hasUpdate) {
                IconButton(minHeight = 35.dp, minWidth = 35.dp, backgroundColor = updateBg, enabled = !module.remove, modifier = Modifier.padding(end = 8.dp), onClick = onUpdate) {
                    Icon(Icons.Outlined.Download, stringResource(R.string.module_update), Modifier.size(20.dp), tint = updateTint)
                }
            }
            IconButton(
                minHeight = 35.dp, minWidth = 35.dp,
                backgroundColor = secondaryContainer,
                onClick = onUninstall,
            ) {
                Icon(
                    if (module.remove) Icons.AutoMirrored.Rounded.Undo else Icons.Outlined.Delete,
                    stringResource(R.string.uninstall),
                    Modifier.size(20.dp),
                    tint = actionIconTint,
                )
            }
        }
    }
}
