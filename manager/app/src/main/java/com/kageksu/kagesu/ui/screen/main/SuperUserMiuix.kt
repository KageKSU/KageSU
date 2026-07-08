// Miuix UI originally authored by YuKongA (https://github.com/YuKongA) for SukiSU-Ultra
// (https://github.com/SukiSU-Ultra/SukiSU-Ultra), GPL-3.0. Original author retains
// copyright; adapted for KageSU on the ReSukiSU base. See docs/ATTRIBUTION.md.

package com.kageksu.kagesu.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.kageksu.kagesu.Natives
import com.kageksu.kagesu.R
import com.kageksu.kagesu.ksuApp
import com.kageksu.kagesu.ui.component.miuix.StatusTag
import com.kageksu.kagesu.ui.navigation.LocalNavigator
import com.kageksu.kagesu.ui.navigation.Route
import com.kageksu.kagesu.ui.theme.LocalEnableBlur
import com.kageksu.kagesu.ui.theme.isInDarkTheme
import com.kageksu.kagesu.ui.util.BlurredBar
import com.kageksu.kagesu.ui.util.LocalSnackbarHost
import com.kageksu.kagesu.ui.util.module.ModuleModify
import com.kageksu.kagesu.ui.viewmodel.AppCategory
import com.kageksu.kagesu.ui.viewmodel.SortType
import com.kageksu.kagesu.ui.viewmodel.SuperUserViewModel
import kotlinx.coroutines.launch
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
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import com.kageksu.kagesu.ui.util.rememberBlurBackdrop

/**
 * Miuix rendering of the SuperUser page. SukiSU-Ultra (GPL-3.0) card/chrome design,
 * wired to ReSukiSU's [SuperUserViewModel] and keeping ReSukiSU's richer feature set
 * (search, sort, category filter, allowlist backup/restore, sulog).
 */
@Composable
fun SuperUserMiuix(bottomPadding: Dp) {
    val context = LocalContext.current
    val viewModel = viewModel<SuperUserViewModel>(viewModelStoreOwner = ksuApp)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.current
    val snackBarHostState = LocalSnackbarHost.current
    val layoutDirection = LocalLayoutDirection.current

    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val barColor = if (backdrop != null) Color.Transparent else colorScheme.surface

    val backupLauncher = ModuleModify.rememberAllowlistBackupLauncher(context, snackBarHostState)
    val restoreLauncher = ModuleModify.rememberAllowlistRestoreLauncher(context, snackBarHostState)

    LaunchedEffect(Unit) { viewModel.updateSearch("") }

    val appCounts = remember(uiState.appGroupList) {
        mapOf(
            AppCategory.ALL to uiState.appGroupList.size,
            AppCategory.ROOT to uiState.appGroupList.count { it.allowSu },
            AppCategory.CUSTOM to uiState.appGroupList.count { !it.allowSu && it.hasCustomProfile },
            AppCategory.DEFAULT to uiState.appGroupList.count { !it.allowSu && !it.hasCustomProfile }
        )
    }

    Scaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.superuser),
                    navigationIcon = {
                        IconButton(onClick = { navigator.push(Route.Sulog) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Article,
                                contentDescription = stringResource(R.string.sulog),
                                tint = colorScheme.onSurface,
                            )
                        }
                    },
                    actions = {
                        SortPopup(
                            current = uiState.currentSortType,
                            onSelect = viewModel::updateCurrentSortType,
                        )
                        MorePopup(
                            showSystemApps = uiState.showSystemApps,
                            onToggleSystemApps = { viewModel.updateShowSystemApps(!uiState.showSystemApps) },
                            onRefresh = { viewModel.viewModelScope.launch { viewModel.fetchAppList() } },
                            onBackup = { backupLauncher.launch(ModuleModify.createAllowlistBackupIntent()) },
                            onRestore = { restoreLauncher.launch(ModuleModify.createAllowlistRestoreIntent()) },
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },

        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        val topPad = innerPadding.calculateTopPadding()
        val startPad = innerPadding.calculateStartPadding(layoutDirection)
        val endPad = innerPadding.calculateEndPadding(layoutDirection)
        val pullState = rememberPullToRefreshState()

        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            PullToRefresh(
                isRefreshing = uiState.isRefreshing,
                pullToRefreshState = pullState,
                onRefresh = { scope.launch { viewModel.fetchAppList() } },
                contentPadding = PaddingValues(top = topPad + 6.dp, start = startPad, end = endPad),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxHeight()
                        .scrollEndHaptic()
                        .overScrollVertical()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(top = topPad + 6.dp, start = startPad, end = endPad),
                    overscrollEffect = null,
                ) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                            var searchField by remember { mutableStateOf(TextFieldValue(uiState.search)) }
                            LaunchedEffect(uiState.search) {
                                if (uiState.search != searchField.text) {
                                    searchField = TextFieldValue(
                                        uiState.search,
                                        selection = TextRange(uiState.search.length)
                                    )
                                }
                            }
                            TextField(
                                value = searchField,
                                onValueChange = {
                                    searchField = it
                                    viewModel.updateSearch(it.text)
                                },
                                label = stringResource(R.string.search_apps),
                                useLabelAsPlaceholder = true,
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceContainerVariant,
                                        modifier = Modifier.padding(start = 12.dp).size(20.dp),
                                    )
                                },
                            )
                            Spacer(Modifier.height(10.dp))
                            CategoryChips(
                                selected = uiState.selectedCategory,
                                counts = appCounts,
                                onSelect = viewModel::updateSelectedCategory,
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    if (uiState.appGroupList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (uiState.isRefreshing && uiState.search.isEmpty()) {
                                    InfiniteProgressIndicator()
                                } else {
                                    Text(
                                        text = if (uiState.search.isNotEmpty() || uiState.selectedCategory == AppCategory.ALL) {
                                            stringResource(R.string.no_apps_found)
                                        } else {
                                            stringResource(R.string.no_apps_in_category)
                                        },
                                        color = colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            uiState.appGroupList,
                            key = { "${it.uid}-${it.mainApp.packageName}" },
                            contentType = { "group" },
                        ) { appGroup ->
                            GroupItem(appGroup = appGroup) {
                                navigator.push(Route.AppProfile(appGroup))
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(bottomPadding + innerPadding.calculateBottomPadding() + 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SortPopup(
    current: SortType,
    onSelect: (SortType) -> Unit,
) {
    Box {
        val show = remember { mutableStateOf(false) }
        OverlayListPopup(
            show = show.value,
            popupPositionProvider = ListPopupDefaults.DropdownPositionProvider,
            alignment = PopupPositionProvider.Align.TopEnd,
            onDismissRequest = { show.value = false },
        ) {
            val entries = SortType.entries
            ListPopupColumn {
                entries.forEachIndexed { index, type ->
                    DropdownImpl(
                        text = stringResource(type.displayNameRes),
                        optionSize = entries.size,
                        isSelected = current == type,
                        index = index,
                        onSelectedIndexChange = {
                            onSelect(type)
                            show.value = false
                        },
                    )
                }
            }
        }
        IconButton(onClick = { show.value = true }, holdDownState = show.value) {
            Icon(
                imageVector = Icons.Filled.Sort,
                contentDescription = stringResource(R.string.sort_options),
                tint = colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun MorePopup(
    showSystemApps: Boolean,
    onToggleSystemApps: () -> Unit,
    onRefresh: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
) {
    Box {
        val show = remember { mutableStateOf(false) }
        val entries = listOf(
            R.string.refresh to onRefresh,
            (if (showSystemApps) R.string.hide_system_apps else R.string.show_system_apps) to onToggleSystemApps,
            R.string.backup_allowlist to onBackup,
            R.string.restore_allowlist to onRestore,
        )
        OverlayListPopup(
            show = show.value,
            popupPositionProvider = ListPopupDefaults.DropdownPositionProvider,
            alignment = PopupPositionProvider.Align.TopEnd,
            onDismissRequest = { show.value = false },
        ) {
            ListPopupColumn {
                entries.forEachIndexed { index, (res, action) ->
                    DropdownImpl(
                        text = stringResource(res),
                        optionSize = entries.size,
                        isSelected = false,
                        index = index,
                        onSelectedIndexChange = {
                            action()
                            show.value = false
                        },
                    )
                }
            }
        }
        IconButton(onClick = { show.value = true }, holdDownState = show.value) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.settings),
                tint = colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CategoryChips(
    selected: AppCategory,
    counts: Map<AppCategory, Int>,
    onSelect: (AppCategory) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(AppCategory.entries.toTypedArray()) { category ->
            val isSelected = selected == category
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) colorScheme.primary else colorScheme.surfaceContainerHigh)
                    .clickable { onSelect(category) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            ) {
                Text(
                    text = "${stringResource(category.displayNameRes)} ${counts[category] ?: 0}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
private fun GroupItem(
    appGroup: SuperUserViewModel.AppGroup,
    onClick: () -> Unit,
) {
    val isDark = isInDarkTheme()
    val bg = colorScheme.secondaryContainer.copy(alpha = 0.8f)
    val fg = colorScheme.onSecondaryContainer
    val rootBg = colorScheme.tertiaryContainer.copy(alpha = 0.6f)
    val rootFg = colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
    val umountBg = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.3f)
    val umountFg = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.8f)
    val defBg = colorScheme.primary.copy(alpha = 0.15f)
    val defFg = colorScheme.primary

    val mainApp = appGroup.mainApp
    val multi = appGroup.apps.size > 1

    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
        onClick = onClick,
        showIndication = true,
        insideMargin = PaddingValues(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(mainApp.packageInfo)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = mainApp.label,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(48.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mainApp.label,
                    modifier = Modifier.basicMarquee(),
                    fontWeight = FontWeight(550),
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                )
                Text(
                    text = if (multi) {
                        stringResource(R.string.group_contains_apps, appGroup.apps.size)
                    } else {
                        mainApp.packageName
                    },
                    modifier = Modifier.basicMarquee(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight(550),
                    color = colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            Column(
                modifier = Modifier.padding(start = 12.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (appGroup.allowSu) {
                    StatusTag("ROOT", rootBg, rootFg)
                } else if (Natives.uidShouldUmount(appGroup.uid)) {
                    StatusTag("UMOUNT", umountBg, umountFg)
                }
                if (appGroup.hasCustomProfile) {
                    StatusTag("CUSTOM", bg, fg)
                } else if (!appGroup.allowSu) {
                    StatusTag("DEFAULT", defBg, defFg)
                }
                if (multi) {
                    appGroup.userName?.let { StatusTag(it, bg, fg) }
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariantActions,
                modifier = Modifier.padding(start = 8.dp).size(22.dp),
            )
        }
    }
}
