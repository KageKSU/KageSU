// Miuix UI adapted from SukiSU-Ultra (https://github.com/SukiSU-Ultra/SukiSU-Ultra),
// GPL-3.0; the original authors retain copyright. Adapted for KageSU on the ReSukiSU base.

package com.kageksu.kagesu.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.kageksu.kagesu.R
import com.kageksu.kagesu.ui.component.ConfirmResult
import com.kageksu.kagesu.ui.component.rememberConfirmDialog
import com.kageksu.kagesu.ui.navigation.LocalNavigator
import com.kageksu.kagesu.ui.theme.LocalEnableBlur
import com.kageksu.kagesu.ui.util.BlurredBar
import com.kageksu.kagesu.ui.util.LocalSnackbarHost
import com.kageksu.kagesu.ui.util.rememberBlurBackdrop
import com.kageksu.kagesu.ui.viewmodel.DynamicManagerViewModel
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/** Miuix rendering of the dynamic-manager screen. Reuses the Material viewModel + manual dialog. */
@Composable
fun DynamicManagerMiuix() {
    val navigator = LocalNavigator.current
    val viewModel = viewModel<DynamicManagerViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val confirmDialog = rememberConfirmDialog()
    val layoutDirection = LocalLayoutDirection.current

    val grantConfirmTitle = stringResource(R.string.dynamic_manager_grant_confirm_title)
    val grantConfirmMessage = stringResource(R.string.dynamic_manager_grant_confirm_message)
    val clearConfirmTitle = stringResource(R.string.dynamic_manager_clear_confirm_title)
    val clearConfirmMessage = stringResource(R.string.dynamic_manager_clear_confirm_message)
    val setSuccess = stringResource(R.string.dynamic_manager_set_success)
    val setFailed = stringResource(R.string.dynamic_manager_set_failed)
    val clearSuccess = stringResource(R.string.dynamic_manager_disabled_success)
    val clearFailed = stringResource(R.string.dynamic_manager_clear_failed)
    val confirmText = stringResource(R.string.confirm)

    fun runGrant(operation: suspend () -> Boolean) {
        scope.launch {
            if (confirmDialog.awaitConfirm(title = grantConfirmTitle, content = grantConfirmMessage, confirm = confirmText) != ConfirmResult.Confirmed) return@launch
            val ok = operation()
            if (ok) viewModel.refresh()
            snackbarHost.showSnackbar(if (ok) setSuccess else setFailed)
        }
    }

    fun runClear() {
        scope.launch {
            if (confirmDialog.awaitConfirm(title = clearConfirmTitle, content = clearConfirmMessage, confirm = confirmText) != ConfirmResult.Confirmed) return@launch
            val ok = viewModel.clearConfig()
            if (ok) viewModel.refresh()
            snackbarHost.showSnackbar(if (ok) clearSuccess else clearFailed)
        }
    }

    val manualDialog = rememberDynamicManagerManualDialog { size, hash ->
        runGrant { viewModel.setManualConfig(size, hash) }
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

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.dynamic_manager_title),
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(MiuixIcons.Back, null, tint = colorScheme.onBackground)
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    bottomContent = {
                        TextField(
                            value = searchField,
                            onValueChange = { searchField = it; viewModel.updateSearch(it.text) },
                            label = stringResource(R.string.search_apps),
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
        val contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            start = innerPadding.calculateStartPadding(layoutDirection),
            end = innerPadding.calculateEndPadding(layoutDirection),
            bottom = innerPadding.calculateBottomPadding() + 16.dp,
        )
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()), Alignment.Center) { InfiniteProgressIndicator() }
        } else {
            val pullState = rememberPullToRefreshState()
            PullToRefresh(
                isRefreshing = uiState.isRefreshing,
                pullToRefreshState = pullState,
                onRefresh = { scope.launch { viewModel.refresh() } },
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
                            val config = uiState.config
                            val currentStatus = if (config?.isValid() == true) {
                                stringResource(R.string.dynamic_manager_enabled_summary, config.size.toString())
                            } else {
                                stringResource(R.string.dynamic_manager_disabled)
                            }
                            Card(modifier = Modifier.fillMaxWidth()) {
                                BasicComponent(
                                    title = stringResource(R.string.dynamic_manager_current_status),
                                    summary = currentStatus,
                                    startAction = { Icon(Icons.Filled.Security, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground) },
                                )
                                if (config?.isValid() == true) {
                                    BasicComponent(
                                        title = stringResource(R.string.signature_hash),
                                        summary = config.hash.orEmpty(),
                                        startAction = { Icon(Icons.Filled.Security, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground) },
                                    )
                                }
                                BasicComponent(
                                    title = stringResource(R.string.dynamic_manager_manual_config),
                                    summary = stringResource(R.string.dynamic_manager_manual_config_summary),
                                    startAction = { Icon(Icons.Filled.Edit, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground) },
                                    onClick = if (!uiState.isSubmitting) ({ manualDialog.show() }) else null,
                                )
                                BasicComponent(
                                    title = stringResource(R.string.dynamic_manager_clear_config),
                                    summary = stringResource(R.string.dynamic_manager_clear_config_summary),
                                    startAction = { Icon(Icons.Filled.Delete, null, Modifier.padding(end = 6.dp), tint = colorScheme.onBackground) },
                                    onClick = if (!uiState.isSubmitting) ({ runClear() }) else null,
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            SmallTitle(stringResource(R.string.manage_managers))
                        }

                        if (uiState.apps.isEmpty()) {
                            item { Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), Alignment.Center) { InfiniteProgressIndicator() } }
                        } else {
                            items(uiState.apps, key = { "${it.uid}-${it.packageName}" }) { app ->
                                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                    BasicComponent(
                                        title = app.label,
                                        summary = if (!app.isChangeable) {
                                            stringResource(
                                                R.string.dynamic_manager_fixed_manager_summary,
                                                app.packageName,
                                                when (app.managerSignatureIndex ?: 0) {
                                                    254 -> "Debug"
                                                    253 -> "KernelSU Toolkit"
                                                    else -> "Kernel"
                                                }
                                            )
                                        } else app.packageName,
                                        startAction = {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current).data(app.packageInfo).crossfade(true).memoryCachePolicy(CachePolicy.ENABLED).build(),
                                                contentDescription = app.label,
                                                modifier = Modifier.padding(end = 8.dp).size(40.dp),
                                            )
                                        },
                                        endActions = {
                                            if (app.isSelected || !app.isChangeable) {
                                                Icon(Icons.Filled.CheckCircle, null, Modifier.size(22.dp), tint = colorScheme.primary)
                                            }
                                        },
                                        onClick = if (app.isChangeable) ({
                                            if (app.isSelected) runClear() else runGrant { viewModel.setManagerApp(app) }
                                        }) else null,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
