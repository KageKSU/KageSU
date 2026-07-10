package com.resukisu.resukisu.ui.susfs

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resukisu.resukisu.R
import com.resukisu.resukisu.ui.susfs.util.SuSFSManager
import com.resukisu.resukisu.ui.susfs.util.SuSFSManager.isSusVersion158
import com.resukisu.resukisu.ui.susfs.util.SuSFSManager.isSusVersion159
import com.resukisu.resukisu.ui.util.isAbDevice
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme

/**
 * Native-miuix rendering of the SuSFS "Basic settings" tab. Same params as the
 * Material [BasicSettingsContent]; wired to ReSukiSU's SuSFSManager. Miuix cards +
 * preference widgets replace the MD3 cards/switches/text-fields/dropdown.
 */
@Composable
fun BasicSettingsContentMiuix(
    unameValue: String,
    onUnameValueChange: (String) -> Unit,
    buildTimeValue: String,
    onBuildTimeValueChange: (String) -> Unit,
    executeInPostFsData: Boolean,
    onExecuteInPostFsDataChange: (Boolean) -> Unit,
    autoStartEnabled: Boolean,
    canEnableAutoStart: Boolean,
    isLoading: Boolean,
    onAutoStartToggle: (Boolean) -> Unit,
    onShowSlotInfo: () -> Unit,
    context: Context,
    onShowBackupDialog: () -> Unit,
    onShowRestoreDialog: () -> Unit,
    enableHideBl: Boolean,
    onEnableHideBlChange: (Boolean) -> Unit,
    enableCleanupResidue: Boolean,
    onEnableCleanupResidueChange: (Boolean) -> Unit,
    enableAvcLogSpoofing: Boolean,
    onEnableAvcLogSpoofingChange: (Boolean) -> Unit,
    hideSusMountsForAllProcs: Boolean,
    onHideSusMountsForAllProcsChange: (Boolean) -> Unit,
) {
    val isAbDevice = produceState(initialValue = false) { value = isAbDevice() }.value
    val isSusVersion159 = isSusVersion159()
    val isSusVersion158 = isSusVersion158()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.susfs_config_description),
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.susfs_config_description_text),
                    color = colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = unameValue,
                    onValueChange = onUnameValueChange,
                    label = stringResource(R.string.susfs_uname_label),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = buildTimeValue,
                    onValueChange = onBuildTimeValueChange,
                    label = stringResource(R.string.susfs_build_time_label),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            OverlayDropdownPreference(
                title = stringResource(R.string.susfs_execution_location_label),
                items = listOf(
                    stringResource(R.string.susfs_execution_location_service),
                    stringResource(R.string.susfs_execution_location_post_fs_data),
                ),
                selectedIndex = if (executeInPostFsData) 1 else 0,
                onSelectedIndexChange = { onExecuteInPostFsDataChange(it == 1) }
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.susfs_current_value, SuSFSManager.getUnameValue(context)),
                    color = colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = stringResource(R.string.susfs_current_build_time, SuSFSManager.getBuildTimeValue(context)),
                    color = colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = stringResource(
                        R.string.susfs_current_execution_location,
                        if (SuSFSManager.getExecuteInPostFsData(context)) "Post-FS-Data" else "Service"
                    ),
                    color = colorScheme.onSurfaceVariantSummary,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            SwitchPreference(
                title = stringResource(R.string.susfs_autostart_title),
                summary = if (canEnableAutoStart) {
                    stringResource(R.string.susfs_autostart_description)
                } else {
                    stringResource(R.string.susfs_autostart_requirement)
                },
                checked = autoStartEnabled,
                onCheckedChange = onAutoStartToggle
            )
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            SwitchPreference(
                title = stringResource(R.string.hide_bl_script),
                summary = stringResource(R.string.hide_bl_script_description),
                checked = enableHideBl,
                onCheckedChange = onEnableHideBlChange
            )
            SwitchPreference(
                title = stringResource(R.string.cleanup_residue),
                summary = stringResource(R.string.cleanup_residue_description),
                checked = enableCleanupResidue,
                onCheckedChange = onEnableCleanupResidueChange
            )
            if (isSusVersion159) {
                SwitchPreference(
                    title = stringResource(R.string.avc_log_spoofing),
                    summary = stringResource(R.string.avc_log_spoofing_description),
                    checked = enableAvcLogSpoofing,
                    onCheckedChange = onEnableAvcLogSpoofingChange
                )
            }
        }

        if (isSusVersion158) {
            Card(modifier = Modifier.fillMaxWidth()) {
                SwitchPreference(
                    title = stringResource(R.string.susfs_hide_mounts_for_all_procs_label),
                    summary = if (hideSusMountsForAllProcs) {
                        stringResource(R.string.susfs_hide_mounts_for_all_procs_enabled_description)
                    } else {
                        stringResource(R.string.susfs_hide_mounts_for_all_procs_disabled_description)
                    },
                    checked = hideSusMountsForAllProcs,
                    onCheckedChange = onHideSusMountsForAllProcsChange
                )
            }
        }

        if (isAbDevice) {
            Card(modifier = Modifier.fillMaxWidth()) {
                ArrowPreference(
                    title = stringResource(R.string.susfs_slot_info_title),
                    summary = stringResource(R.string.susfs_slot_info_description),
                    onClick = onShowSlotInfo
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                text = stringResource(R.string.susfs_backup_title),
                onClick = onShowBackupDialog,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                text = stringResource(R.string.restore),
                onClick = onShowRestoreDialog,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
