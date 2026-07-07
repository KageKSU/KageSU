package com.kageksu.kagesu.ui.screen.moduleRepo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material.icons.outlined.WebAsset
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kageksu.kagesu.R
import com.kageksu.kagesu.ui.activity.util.isNetworkAvailable
import com.kageksu.kagesu.ui.component.ConfirmResult
import com.kageksu.kagesu.ui.component.miuix.StatusTag
import com.kageksu.kagesu.ui.component.rememberConfirmDialog
import com.kageksu.kagesu.ui.component.rememberCustomDialog
import com.kageksu.kagesu.ui.navigation.LocalNavigator
import com.kageksu.kagesu.ui.navigation.Route
import com.kageksu.kagesu.data.appPreferences
import com.kageksu.kagesu.ui.theme.LocalEnableBlur
import com.kageksu.kagesu.ui.util.BlurredBar
import com.kageksu.kagesu.ui.util.LocalPermissionRequestInterface
import com.kageksu.kagesu.ui.util.LocalSnackbarHost
import com.kageksu.kagesu.ui.util.rememberBlurBackdrop
import com.kageksu.kagesu.ui.viewmodel.ModuleRepoViewModel
import com.kageksu.kagesu.ui.viewmodel.ModuleRepoViewModel.RepoModule
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
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
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/** Miuix rendering of the online module repository. Reuses the Material VM + install flow. */
@Composable
fun ModuleRepoMiuix() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val prefs = context.appPreferences
    val viewModel = viewModel<ModuleRepoViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHost = LocalSnackbarHost.current
    val layoutDirection = LocalLayoutDirection.current
    val confirmDialog = rememberConfirmDialog()
    val currentModuleForChooseDialog = remember { mutableStateOf<RepoModule?>(null) }
    val chooseDialog = rememberCustomDialog { dismiss ->
        ChooseDialogContent(currentModuleForChooseDialog, viewModel, dismiss)
    }

    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val barColor = if (backdrop != null) Color.Transparent else colorScheme.surface

    var searchField by remember { mutableStateOf(TextFieldValue(uiState.search)) }
    LaunchedEffect(uiState.search) {
        if (uiState.search != searchField.text) {
            searchField = TextFieldValue(uiState.search, selection = TextRange(uiState.search.length))
        }
    }
    LaunchedEffect(Unit) {
        viewModel.setSortStargazerCountFirst(prefs.getBoolean("module_repo_sort_star_first", false))
    }

    val isLoading = uiState.modules.isEmpty()
    var offline by remember { mutableStateOf(!isNetworkAvailable(context)) }

    Scaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.module_repo),
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(MiuixIcons.Back, null, tint = colorScheme.onBackground)
                        }
                    },
                    actions = {
                        RepoSortPopup(
                            starFirst = uiState.sortStargazerCountFirst,
                            onToggle = {
                                val v = !uiState.sortStargazerCountFirst
                                viewModel.setSortStargazerCountFirst(v)
                                prefs.putBoolean("module_repo_sort_star_first", v)
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
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 6.dp),
                            leadingIcon = { Icon(Icons.Filled.Search, null, Modifier.padding(start = 12.dp).size(20.dp), tint = colorScheme.onSurfaceContainerVariant) },
                        )
                    },
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()), Alignment.Center) {
                if (offline) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.SignalWifiOff, null, Modifier.size(48.dp), tint = colorScheme.onSurfaceVariantSummary)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.network_offline), color = colorScheme.onSurface)
                        Spacer(Modifier.height(2.dp))
                        Text(stringResource(R.string.please_check_network), color = colorScheme.onSurfaceVariantSummary)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { offline = false; viewModel.refresh(onFailure = { offline = true }) }) {
                            Text(stringResource(R.string.network_retry))
                        }
                    }
                } else {
                    InfiniteProgressIndicator()
                    viewModel.refresh(onFailure = { offline = true })
                }
            }
        } else {
            val pullState = rememberPullToRefreshState()
            val contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                start = innerPadding.calculateStartPadding(layoutDirection),
                end = innerPadding.calculateEndPadding(layoutDirection),
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            )
            PullToRefresh(
                isRefreshing = uiState.isRefreshing,
                pullToRefreshState = pullState,
                onRefresh = { viewModel.refresh() },
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
                            .padding(horizontal = 12.dp),
                        contentPadding = contentPadding,
                        overscrollEffect = null,
                    ) {
                        items(uiState.modules, key = { it.moduleId }) { module ->
                            OnlineModuleItemMiuix(module, viewModel, confirmDialog, chooseDialog, currentModuleForChooseDialog)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepoSortPopup(starFirst: Boolean, onToggle: () -> Unit) {
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
                    text = stringResource(R.string.module_sort_star_first),
                    optionSize = 1, isSelected = starFirst, index = 0,
                    onSelectedIndexChange = { onToggle(); show.value = false },
                )
            }
        }
        IconButton(onClick = { show.value = true }, holdDownState = show.value) {
            Icon(Icons.Filled.MoreVert, stringResource(R.string.settings), tint = colorScheme.onSurface)
        }
    }
}

@Composable
private fun OnlineModuleItemMiuix(
    module: RepoModule,
    viewModel: ModuleRepoViewModel,
    confirmDialog: com.kageksu.kagesu.ui.component.ConfirmDialogHandle,
    chooseDialog: com.kageksu.kagesu.ui.component.DialogHandle,
    currentModuleForChooseDialog: androidx.compose.runtime.MutableState<RepoModule?>,
) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val permissionRequestInterface = LocalPermissionRequestInterface.current

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        insideMargin = PaddingValues(16.dp),
        onClick = { navigator.push(Route.ModuleRepoDetail(module)) },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = module.moduleName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (module.stargazerCount > 0) {
                Icon(Icons.Rounded.Star, null, Modifier.padding(start = 6.dp).size(16.dp), tint = colorScheme.onSurfaceVariantSummary)
                Text(module.stargazerCount.toString(), fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary, modifier = Modifier.padding(start = 3.dp))
            }
        }
        Text("${stringResource(R.string.module_version)}: ${module.latestRelease} (${module.latestVersionCode})", fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
        Text("${stringResource(R.string.module_author)}: ${module.authors}", fontSize = 12.sp, color = colorScheme.onSurfaceVariantSummary)
        Spacer(Modifier.height(10.dp))
        Text(module.summary, fontSize = 13.sp, color = colorScheme.onSurfaceVariantSummary, maxLines = 4, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            StatusTag(module.moduleId, colorScheme.primary.copy(alpha = 0.15f), colorScheme.primary)
            if (module.metamodule) StatusTag("META", colorScheme.tertiaryContainer, colorScheme.onTertiaryContainer)
            if (module.installed) StatusTag(stringResource(R.string.installed), colorScheme.secondaryContainer, colorScheme.onSecondaryContainer)
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(thickness = 0.5.dp, color = colorScheme.outline.copy(alpha = 0.5f))
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(Modifier.weight(1f))
            IconButton(minHeight = 35.dp, minWidth = 35.dp, backgroundColor = colorScheme.secondaryContainer.copy(0.8f), onClick = { navigator.push(Route.ModuleRepoDetail(module)) }) {
                Icon(Icons.Outlined.WebAsset, null, Modifier.size(20.dp), tint = colorScheme.onSurface)
            }
            val latestAsset = module.latestAsset
            if (latestAsset != null) {
                val confirmInstallTitle = stringResource(R.string.confirm_install_module_title, module.moduleName)
                IconButton(minHeight = 35.dp, minWidth = 35.dp, backgroundColor = colorScheme.tertiaryContainer.copy(0.6f), onClick = {
                    viewModel.viewModelScope.launch {
                        if (confirmDialog.awaitConfirm(title = confirmInstallTitle, html = true, content = latestAsset.descriptionHTML) == ConfirmResult.Canceled) return@launch
                        val assets = latestAsset.assets
                        if (assets.size <= 1) {
                            assets.firstOrNull()?.let { asset ->
                                downloadAssetAndInstall(context, permissionRequestInterface, module, asset, navigator, viewModel.viewModelScope)
                            }
                        } else {
                            currentModuleForChooseDialog.value = module
                            chooseDialog.show()
                        }
                    }
                }) {
                    Icon(Icons.Outlined.Download, null, Modifier.size(20.dp), tint = colorScheme.onTertiaryContainer)
                }
            }
        }
    }
}
