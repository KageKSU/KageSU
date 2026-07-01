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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kageksu.kagesu.R
import com.kageksu.kagesu.ui.component.KsuIsValid
import com.kageksu.kagesu.ui.component.material.SegmentedColumn
import com.kageksu.kagesu.ui.component.material.SegmentedListItem

@Composable
fun BackupRestoreMaterial(
    isBusy: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.config_backup_title)) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 16.dp),
            contentPadding = innerPadding,
        ) {
            item {
                if (isBusy) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                KsuIsValid {
                    SegmentedColumn(
                        modifier = Modifier.padding(top = 12.dp),
                        content = listOf(
                            {
                                val exportTitle = stringResource(R.string.config_backup_export)
                                SegmentedListItem(
                                    onClick = { if (!isBusy) onExport() },
                                    headlineContent = { Text(exportTitle) },
                                    supportingContent = { Text(stringResource(R.string.config_backup_export_summary)) },
                                    leadingContent = {
                                        Icon(
                                            Icons.Rounded.Backup,
                                            exportTitle,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                )
                            },
                            {
                                val importTitle = stringResource(R.string.config_backup_import)
                                SegmentedListItem(
                                    onClick = { if (!isBusy) onImport() },
                                    headlineContent = { Text(importTitle) },
                                    supportingContent = { Text(stringResource(R.string.config_backup_import_summary)) },
                                    leadingContent = {
                                        Icon(
                                            Icons.Rounded.Restore,
                                            importTitle,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                )
                            }
                        )
                    )
                }
            }
        }
    }
}
