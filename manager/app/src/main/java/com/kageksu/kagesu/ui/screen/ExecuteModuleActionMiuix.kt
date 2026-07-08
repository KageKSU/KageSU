// Miuix UI originally authored by YuKongA (https://github.com/YuKongA) for SukiSU-Ultra
// (https://github.com/SukiSU-Ultra/SukiSU-Ultra), GPL-3.0. Original author retains
// copyright; adapted for KageSU on the ReSukiSU base. See docs/ATTRIBUTION.md.

package com.kageksu.kagesu.ui.screen

import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kageksu.kagesu.R
import com.kageksu.kagesu.ui.navigation.LocalNavigator
import com.kageksu.kagesu.ui.theme.LocalEnableBlur
import com.kageksu.kagesu.ui.util.BlurredBar
import com.kageksu.kagesu.ui.util.LocalSnackbarHost
import com.kageksu.kagesu.ui.util.rememberBlurBackdrop
import com.kageksu.kagesu.ui.util.runModuleAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Miuix rendering of the module action-output screen. Reuses runModuleAction. */
@Composable
fun ExecuteModuleActionMiuix(moduleId: String) {
    var text by rememberSaveable { mutableStateOf("") }
    val logContent = remember { StringBuilder() }
    val snackBarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var isActionRunning by rememberSaveable { mutableStateOf(true) }
    val context = LocalContext.current
    val activity = LocalActivity.current
    val navigator = LocalNavigator.current

    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val barColor = if (backdrop != null) Color.Transparent else colorScheme.surface

    BackHandler(enabled = isActionRunning) { /* block back while running */ }

    val fromShortcut = remember(activity) {
        activity?.intent?.getStringExtra("shortcut_type") == "module_action"
    }

    LaunchedEffect(Unit) {
        if (text.isNotEmpty()) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            runModuleAction(
                moduleId = moduleId,
                onStdout = {
                    val tempText = "$it\n"
                    if (tempText.startsWith("[H[J")) {
                        text = tempText.substring(6)
                    } else {
                        text += tempText
                    }
                    logContent.append(it).append("\n")
                },
                onStderr = { logContent.append(it).append("\n") },
            ).let { ok ->
                if (ok && fromShortcut) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.module_action_success), Toast.LENGTH_SHORT).show()
                        activity?.finishAndRemoveTask()
                    }
                }
            }
        }
        isActionRunning = false
    }

    Scaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.action),
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(MiuixIcons.Back, null, tint = colorScheme.onBackground)
                        }
                    },
                    actions = {
                        if (!isActionRunning) {
                            IconButton(onClick = {
                                scope.launch {
                                    val date = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date())
                                    val file = File(
                                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                        "KernelSU_module_action_log_${date}.log"
                                    )
                                    file.writeText(logContent.toString())
                                    snackBarHost.showSnackbar("Log saved to ${file.absolutePath}")
                                }
                            }) {
                                Icon(Icons.Filled.Save, stringResource(R.string.save_log), tint = colorScheme.onBackground)
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        floatingActionButton = {
            if (!isActionRunning) {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = 20.dp, end = 20.dp),
                    onClick = { navigator.pop() },
                ) {
                    Icon(Icons.Filled.Close, stringResource(R.string.close), tint = colorScheme.onPrimary)
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        LaunchedEffect(text) { listState.animateScrollToItem(2) }
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
            item { Spacer(Modifier.height(innerPadding.calculateTopPadding())) }
            item {
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = text,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onSurface,
                )
            }
            item { Spacer(Modifier.height(innerPadding.calculateBottomPadding() + 80.dp)) }
        }
    }
}
