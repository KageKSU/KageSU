package com.kageksu.kagesu.ui.screen

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kageksu.kagesu.R
import com.kageksu.kagesu.ui.component.ConfirmResult
import com.kageksu.kagesu.ui.component.miuix.StatusTag
import com.kageksu.kagesu.ui.component.miuix.WarningCard
import com.kageksu.kagesu.ui.component.rememberConfirmDialog
import com.kageksu.kagesu.ui.navigation.LocalNavigator
import com.kageksu.kagesu.ui.theme.LocalEnableBlur
import com.kageksu.kagesu.ui.util.BlurredBar
import com.kageksu.kagesu.ui.util.LocalSnackbarHost
import com.kageksu.kagesu.ui.util.rememberBlurBackdrop
import com.kageksu.kagesu.ui.viewmodel.UmountManagerScreenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/** Miuix rendering of the umount-path manager. Reuses the Material viewModel + add dialog. */
@Composable
fun UmountManagerMiuix() {
    val viewModel = viewModel<UmountManagerScreenViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val snackBarHost = LocalSnackbarHost.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val confirmDialog = rememberConfirmDialog()
    val layoutDirection = LocalLayoutDirection.current

    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val barColor = if (backdrop != null) Color.Transparent else colorScheme.surface

    var showAddDialog by remember { mutableStateOf(false) }
    val confirmDelete = stringResource(R.string.confirm_delete)

    LaunchedEffect(Unit) { viewModel.refreshData(context) }

    Scaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.umount_path_manager),
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(MiuixIcons.Back, null, tint = colorScheme.onBackground)
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = 20.dp, end = 20.dp),
                onClick = { showAddDialog = true },
            ) {
                Icon(Icons.Filled.Add, stringResource(R.string.add_umount_path), Modifier.size(30.dp), tint = colorScheme.onPrimary)
            }
        },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        val contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 6.dp,
            start = innerPadding.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection),
            bottom = innerPadding.calculateBottomPadding() + 96.dp,
        )
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()), Alignment.Center) {
                InfiniteProgressIndicator()
            }
        } else {
            val pullState = rememberPullToRefreshState()
            PullToRefresh(
                isRefreshing = uiState.isRefreshing,
                pullToRefreshState = pullState,
                onRefresh = { viewModel.markUmountPathDirty(); viewModel.refreshData(context) },
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
                        item {
                            WarningCard(
                                message = stringResource(R.string.changes_take_effect_immediately),
                                color = colorScheme.primaryContainer,
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        if (uiState.umountPaths.isEmpty()) {
                            item {
                                WarningCard(
                                    message = stringResource(R.string.no_any_umount_path),
                                    color = colorScheme.secondaryContainer,
                                )
                            }
                        }
                        items(uiState.umountPaths, key = { it.path }) { entry ->
                            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), insideMargin = PaddingValues(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Folder, null, Modifier.padding(end = 12.dp).size(24.dp), tint = colorScheme.onSurface)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(entry.path, color = colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 2)
                                        Spacer(Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            StatusTag(
                                                label = if (entry.persistent) stringResource(R.string.persistent) else stringResource(R.string.temporary),
                                                backgroundColor = colorScheme.primary.copy(alpha = 0.15f),
                                                contentColor = colorScheme.primary,
                                            )
                                            StatusTag(
                                                label = entry.flagName,
                                                backgroundColor = colorScheme.secondaryContainer,
                                                contentColor = colorScheme.onSecondaryContainer,
                                            )
                                        }
                                    }
                                    val confirmSummary = stringResource(R.string.confirm_delete_umount_path, entry.path)
                                    IconButton(onClick = {
                                        scope.launch {
                                            if (confirmDialog.awaitConfirm(title = confirmDelete, content = confirmSummary) != ConfirmResult.Confirmed) return@launch
                                            withContext(Dispatchers.IO) { viewModel.removePath(entry, snackBarHost, context) }
                                        }
                                    }) {
                                        Icon(Icons.Filled.Delete, null, Modifier.size(22.dp), tint = colorScheme.onSurfaceVariantActions)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddUmountPathDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { path, flags ->
                    showAddDialog = false
                    uiState.umountPaths.filter { it.path == path }.forEach {
                        viewModel.removePath(entry = it, snackBarHost = null, context = null)
                    }
                    viewModel.addPath(path = path, flags = flags, snackBarHost = snackBarHost, context = context)
                }
            )
        }
    }
}
