package com.kageksu.kagesu.ui.screen.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Wysiwyg
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kageksu.kagesu.Natives
import com.kageksu.kagesu.R
import com.kageksu.kagesu.data.appPreferences
import com.kageksu.kagesu.ksuApp
import com.kageksu.kagesu.ui.component.InstallConfirmationDialog
import com.kageksu.kagesu.ui.component.ZipFileDetector.parseModuleInfo
import com.kageksu.kagesu.ui.component.ZipFileInfo
import com.kageksu.kagesu.ui.component.ZipType
import com.kageksu.kagesu.ui.util.module.ModuleUtils
import com.kageksu.kagesu.ui.component.miuix.StatusTag
import com.kageksu.kagesu.ui.navigation.LocalNavigator
import com.kageksu.kagesu.ui.navigation.Route
import com.kageksu.kagesu.ui.screen.FlashIt
import com.kageksu.kagesu.ui.theme.LocalEnableBlur
import com.kageksu.kagesu.ui.util.BlurredBar
import com.kageksu.kagesu.ui.util.LocalSnackbarHost
import com.kageksu.kagesu.ui.util.hasMagisk
import com.kageksu.kagesu.ui.util.rememberBlurBackdrop
import com.kageksu.kagesu.ui.viewmodel.ModuleViewModel
import com.kageksu.kagesu.ui.webui.WebUIActivity
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * Miuix rendering of the Module page. Miuix chrome (collapsing TopAppBar, FAB,
 * scroll haptics) + Miuix module cards, reusing ReSukiSU's ModuleList (all the
 * enable/uninstall/update/shortcut logic) and install flow unchanged. Dispatched
 * from ModulePage / ModuleItem when Miuix; Material untouched.
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
    var lastClickTime by remember { mutableStateOf(0L) }

    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val barColor = if (backdrop != null) Color.Transparent else colorScheme.surface

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
            if (clipData != null) {
                val selected = mutableListOf<Uri>()
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    try {
                        if (!ModuleUtils.isUriAccessible(context, uri)) continue
                        ModuleUtils.takePersistableUriPermission(context, uri)
                        selected.add(uri)
                    } catch (e: Exception) {
                        Log.e("ModuleMiuix", "Error processing URI: $uri, ${e.message}")
                    }
                }
                if (selected.isEmpty()) {
                    snackBarHost.showSnackbar("Unable to access selected module files")
                    return@launch
                }
                selected.forEach { zipFiles.add(parseModuleInfo(context, it)) }
                pendingZipFiles = zipFiles
                showConfirmationDialog = true
            } else {
                val uri = data.data ?: return@launch
                try {
                    if (!ModuleUtils.isUriAccessible(context, uri)) {
                        snackBarHost.showSnackbar("Unable to access selected module files")
                        return@launch
                    }
                    ModuleUtils.takePersistableUriPermission(context, uri)
                    zipFiles.add(parseModuleInfo(context, uri))
                    pendingZipFiles = zipFiles
                    showConfirmationDialog = true
                } catch (e: Exception) {
                    snackBarHost.showSnackbar("Error processing module file: ${e.message}")
                }
            }
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

    val isSafeMode = Natives.isSafeMode
    val hasMagisk = hasMagisk()
    val hideInstallButton = isSafeMode || hasMagisk

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
                                viewModel.setSortActionFirst(v)
                                prefs.putBoolean("module_sort_action_first", v)
                            },
                            onToggleEnabledFirst = {
                                val v = !uiState.sortEnabledFirst
                                viewModel.setSortEnabledFirst(v)
                                prefs.putBoolean("module_sort_enabled_first", v)
                            },
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = (if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    var searchField by remember { mutableStateOf(TextFieldValue(uiState.search)) }
                    LaunchedEffect(uiState.search) {
                        if (uiState.search != searchField.text) {
                            searchField = TextFieldValue(uiState.search, selection = TextRange(uiState.search.length))
                        }
                    }
                    TextField(
                        value = searchField,
                        onValueChange = {
                            searchField = it
                            viewModel.updateSearch(it.text)
                        },
                        label = stringResource(R.string.search_modules),
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search, null,
                                Modifier.padding(start = 12.dp).size(20.dp),
                                tint = colorScheme.onSurfaceContainerVariant,
                            )
                        },
                    )

                    when {
                        hasMagisk -> EmptyState(Icons.Outlined.Warning, stringResource(R.string.module_magisk_conflict))
                        uiState.moduleList.isEmpty() -> EmptyState(Icons.Outlined.Extension, stringResource(R.string.module_empty))
                        else -> ModuleList(
                            viewModel = viewModel,
                            uiState = uiState,
                            listState = listState,
                            modifier = Modifier
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                                .scrollEndHaptic()
                                .overScrollVertical(),
                            onUpdateModule = { navigator.push(Route.Flash(FlashIt.FlashModuleUpdate(it))) },
                            onClickModule = { id, name, hasWebUi ->
                                val now = System.currentTimeMillis()
                                if (now - lastClickTime < 600) return@ModuleList
                                lastClickTime = now
                                if (hasWebUi) {
                                    try {
                                        context.startActivity(
                                            Intent(context, WebUIActivity::class.java)
                                                .setData("kernelsu://webui/$id".toUri())
                                                .putExtra("id", id)
                                                .putExtra("name", name)
                                        )
                                    } catch (e: Exception) {
                                        scope.launch { snackBarHost.showSnackbar("Error launching WebUI: ${e.message}") }
                                    }
                                }
                            },
                            context = context,
                            snackBarHost = snackBarHost,
                            bottomPadding = bottomPadding,
                            topPadding = 0.dp,
                        )
                    }
                }
            }

            if (!hideInstallButton) {
                IconButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = bottomPadding + 20.dp)
                        .size(56.dp),
                    onClick = {
                        selectZipLauncher.launch(
                            Intent(Intent.ACTION_GET_CONTENT).apply {
                                type = "application/zip"
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            }
                        )
                    },
                    backgroundColor = colorScheme.primary,
                ) {
                    Icon(Icons.Filled.Add, stringResource(R.string.install), tint = colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
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
                    optionSize = 2,
                    isSelected = sortActionFirst,
                    index = 0,
                    onSelectedIndexChange = { onToggleActionFirst() },
                )
                DropdownImpl(
                    text = stringResource(R.string.module_sort_enabled_first),
                    optionSize = 2,
                    isSelected = sortEnabledFirst,
                    index = 1,
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
fun ModuleItemMiuix(
    viewModel: ModuleViewModel,
    module: ModuleViewModel.ModuleInfo,
    moduleSizes: Map<String, String>,
    updateUrl: String,
    onUninstallClicked: (ModuleViewModel.ModuleInfo) -> Unit,
    onCheckChanged: (Boolean) -> Unit,
    onUpdate: (ModuleViewModel.ModuleInfo) -> Unit,
    onClick: (ModuleViewModel.ModuleInfo) -> Unit,
    onModuleAddShortcut: (ModuleViewModel.ModuleInfo) -> Unit,
) {
    val navigator = LocalNavigator.current
    val decoration = if (module.remove) TextDecoration.LineThrough else null

    LaunchedEffect(module.dirId) { viewModel.loadSize(module.dirId) }
    val sizeStr = moduleSizes[module.dirId]

    val clickable = module.hasActionScript || module.hasWebUi
    Card(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .run {
                    if (clickable) combinedClickable(
                        onLongClick = { onModuleAddShortcut(module) },
                        onClick = { if (module.hasWebUi) onClick(module) },
                    ) else this
                }
                .padding(20.dp, 16.dp, 20.dp, 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = module.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        textDecoration = decoration,
                    )
                    Text(
                        text = "${stringResource(R.string.module_version)}: ${module.version}",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariantSummary,
                        textDecoration = decoration,
                    )
                    Text(
                        text = "${stringResource(R.string.module_author)}: ${module.author}",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariantSummary,
                        textDecoration = decoration,
                    )
                }
                Switch(
                    checked = module.enabled,
                    enabled = !module.update,
                    onCheckedChange = onCheckChanged,
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = module.description,
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariantSummary,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                textDecoration = decoration,
            )

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusTag(module.dirId, colorScheme.primary.copy(alpha = 0.15f), colorScheme.primary)
                if (module.metamodule) {
                    StatusTag("META", colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer)
                }
                StatusTag(sizeStr ?: "0 KB", colorScheme.secondaryContainer, colorScheme.onSecondaryContainer)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (module.hasActionScript) {
                    IconButton(
                        enabled = !module.remove && module.enabled,
                        onClick = {
                            navigator.push(Route.ExecuteModuleAction(module.dirId))
                            viewModel.markNeedRefresh()
                        },
                    ) { Icon(Icons.Outlined.PlayArrow, null, Modifier.size(20.dp), tint = colorScheme.onSurface) }
                }
                if (module.hasWebUi) {
                    IconButton(
                        enabled = !module.remove && module.enabled,
                        onClick = { onClick(module) },
                    ) { Icon(Icons.AutoMirrored.Outlined.Wysiwyg, null, Modifier.size(20.dp), tint = colorScheme.onSurface) }
                }
                Spacer(Modifier.weight(1f))
                if (updateUrl.isNotEmpty()) {
                    IconButton(
                        enabled = !module.remove,
                        onClick = { onUpdate(module) },
                    ) { Icon(Icons.Outlined.Download, null, Modifier.size(20.dp), tint = colorScheme.primary) }
                }
                IconButton(onClick = { onUninstallClicked(module) }) {
                    if (!module.remove) {
                        Icon(Icons.Outlined.Delete, null, Modifier.size(20.dp), tint = colorScheme.onSurface)
                    } else {
                        Icon(Icons.Outlined.Refresh, null, Modifier.size(20.dp).rotate(180f), tint = colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
