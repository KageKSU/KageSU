package com.kageksu.kagesu.ui.screen.backup

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.kageksu.kagesu.R
import com.kageksu.kagesu.ui.component.KsuIsValid
import com.kageksu.kagesu.ui.theme.LocalEnableBlur
import com.kageksu.kagesu.ui.util.BlurredBar
import com.kageksu.kagesu.ui.util.rememberBlurBackdrop
import com.kageksu.kagesu.ui.util.wallpaperBarColor
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun BackupRestoreMiuix(
    isBusy: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val enableBlur = LocalEnableBlur.current
    val backdrop = rememberBlurBackdrop(enableBlur)
    val blurActive = backdrop != null
    val barColor = wallpaperBarColor(blurActive)

    Scaffold(
        topBar = {
            BlurredBar(backdrop) {
                TopAppBar(
                    color = barColor,
                    title = stringResource(R.string.config_backup_title),
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = { /* handled in navigator */ }) {
                            val layoutDirection = LocalLayoutDirection.current
                            Icon(
                                modifier = Modifier.graphicsLayer {
                                    if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                                },
                                imageVector = MiuixIcons.Back,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
                .fillMaxHeight()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = innerPadding,
            overscrollEffect = null,
        ) {
            item {
                KsuIsValid {
                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                    ) {
                        val exportTitle = stringResource(R.string.config_backup_export)
                        ArrowPreference(
                            title = exportTitle,
                            summary = stringResource(R.string.config_backup_export_summary),
                            enabled = !isBusy,
                            startAction = {
                                Icon(
                                    imageVector = Icons.Rounded.Backup,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = exportTitle,
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = onExport
                        )

                        val importTitle = stringResource(R.string.config_backup_import)
                        ArrowPreference(
                            title = importTitle,
                            summary = stringResource(R.string.config_backup_import_summary),
                            enabled = !isBusy,
                            startAction = {
                                Icon(
                                    imageVector = Icons.Rounded.Restore,
                                    modifier = Modifier.padding(end = 6.dp),
                                    contentDescription = importTitle,
                                    tint = colorScheme.onBackground
                                )
                            },
                            onClick = onImport
                        )
                    }
                }
            }
        }
    }
}
