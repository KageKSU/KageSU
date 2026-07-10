package com.resukisu.resukisu.ui.susfs

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resukisu.resukisu.R
import com.resukisu.resukisu.ui.navigation.LocalNavigator
import com.resukisu.resukisu.ui.susfs.component.AddAppPathDialog
import com.resukisu.resukisu.ui.susfs.component.AddKstatStaticallyDialog
import com.resukisu.resukisu.ui.susfs.component.AddPathDialog
import com.resukisu.resukisu.ui.susfs.component.ConfirmDialog
import com.resukisu.resukisu.ui.susfs.component.EnabledFeaturesContent
import com.resukisu.resukisu.ui.susfs.component.KstatConfigContent
import com.resukisu.resukisu.ui.susfs.component.PathSettingsContent
import com.resukisu.resukisu.ui.susfs.component.SusLoopPathsContent
import com.resukisu.resukisu.ui.susfs.component.SusMapsContent
import com.resukisu.resukisu.ui.susfs.component.SusPathsContent
import com.resukisu.resukisu.ui.susfs.util.SuSFSManager
import com.resukisu.resukisu.ui.susfs.util.SuSFSManager.isSusVersion1512
import com.resukisu.resukisu.ui.susfs.util.SuSFSManager.isSusVersion158
import com.resukisu.resukisu.ui.susfs.util.SuSFSManager.isSusVersion159
import com.resukisu.resukisu.ui.theme.LocalEnableBlur
import com.resukisu.resukisu.ui.util.BlurredBar
import com.resukisu.resukisu.ui.util.getSuSFSVersion
import com.resukisu.resukisu.ui.util.rememberBlurBackdrop
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme as MiuixColorScheme

/**
 * Miuix rendering of ReSukiSU's SuSFS config. SCAFFOLDING STEP: miuix chrome
 * (Scaffold + collapsing TopAppBar + blur + a scrollable miuix-styled tab row +
 * per-tab bottom bar) wrapping ReSukiSU's own state + SuSFSManager backend and,
 * for now, the existing content composables. Per-tab content is being converted
 * to native miuix widgets in follow-up commits. Material path is untouched.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SdCardPath", "AutoboxingStateCreation")
@Composable
fun SuSFSConfigScreenMiuix() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(SuSFSTab.BASIC_SETTINGS) }
    var unameValue by remember { mutableStateOf("") }
    var buildTimeValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showConfirmReset by remember { mutableStateOf(false) }
    var autoStartEnabled by remember { mutableStateOf(false) }
    var executeInPostFsData by remember { mutableStateOf(false) }
    var enableHideBl by remember { mutableStateOf(true) }
    var enableCleanupResidue by remember { mutableStateOf(false) }
    var enableAvcLogSpoofing by remember { mutableStateOf(false) }

    // 槽位信息相关状态
    var slotInfoList by remember { mutableStateOf(emptyList<SuSFSManager.SlotInfo>()) }
    var currentActiveSlot by remember { mutableStateOf("") }
    var isLoadingSlotInfo by remember { mutableStateOf(false) }
    var showSlotInfoDialog by remember { mutableStateOf(false) }

    // 路径管理相关状态
    var susPaths by remember { mutableStateOf(emptySet<String>()) }
    var susLoopPaths by remember { mutableStateOf(emptySet<String>()) }
    var susMaps by remember { mutableStateOf(emptySet<String>()) }
    var androidDataPath by remember { mutableStateOf("") }
    var sdcardPath by remember { mutableStateOf("") }

    // SUS挂载隐藏控制状态
    var hideSusMountsForAllProcs by remember { mutableStateOf(true) }

    // Kstat配置相关状态
    var kstatConfigs by remember { mutableStateOf(emptySet<String>()) }
    var addKstatPaths by remember { mutableStateOf(emptySet<String>()) }

    // 启用功能状态相关
    var enabledFeatures by remember { mutableStateOf(emptyList<SuSFSManager.EnabledFeature>()) }
    var isLoadingFeatures by remember { mutableStateOf(false) }

    // 应用列表相关状态
    var installedApps by remember { mutableStateOf(emptyList<SuSFSManager.AppInfo>()) }

    // 对话框状态
    var showAddPathDialog by remember { mutableStateOf(false) }
    var showAddLoopPathDialog by remember { mutableStateOf(false) }
    var showAddSusMapDialog by remember { mutableStateOf(false) }
    var showAddAppPathDialog by remember { mutableStateOf(false) }
    var showAddKstatStaticallyDialog by remember { mutableStateOf(false) }
    var showAddKstatDialog by remember { mutableStateOf(false) }

    // 编辑状态
    var editingPath by remember { mutableStateOf<String?>(null) }
    var editingLoopPath by remember { mutableStateOf<String?>(null) }
    var editingSusMap by remember { mutableStateOf<String?>(null) }
    var editingKstatConfig by remember { mutableStateOf<String?>(null) }
    var editingKstatPath by remember { mutableStateOf<String?>(null) }

    // 重置确认对话框状态
    var showResetPathsDialog by remember { mutableStateOf(false) }
    var showResetLoopPathsDialog by remember { mutableStateOf(false) }
    var showResetSusMapsDialog by remember { mutableStateOf(false) }
    var showResetKstatDialog by remember { mutableStateOf(false) }

    // 备份还原相关状态
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var selectedBackupFile by remember { mutableStateOf<String?>(null) }
    var backupInfo by remember { mutableStateOf<SuSFSManager.BackupData?>(null) }

    var isNavigating by remember { mutableStateOf(false) }

    val allTabs = SuSFSTab.getAllTabs(isSusVersion158(), isSusVersion159(), isSusVersion1512())

    // 实时判断是否可以启用开机自启动
    val canEnableAutoStart by remember {
        derivedStateOf {
            SuSFSManager.hasConfigurationForAutoStart(context)
        }
    }

    var showVersionMismatchDialog by remember { mutableStateOf(false) }

    if (showVersionMismatchDialog) {
        AlertDialog(
            onDismissRequest = { showVersionMismatchDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.warning),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.susfs_version_mismatch,
                        try { getSuSFSVersion() } catch (_: Exception) { "unknown" },
                        SuSFSManager.MAX_SUSFS_VERSION
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showVersionMismatchDialog = false },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }

    // 文件选择器
    val backupFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { fileUri ->
            val fileName = SuSFSManager.getDefaultBackupFileName()
            val tempFile = File(context.cacheDir, fileName)
            coroutineScope.launch {
                isLoading = true
                val success = SuSFSManager.createBackup(context, tempFile.absolutePath)
                if (success) {
                    try {
                        context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                            tempFile.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    tempFile.delete()
                }
                isLoading = false
                showBackupDialog = false
            }
        }
    }

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { fileUri ->
            coroutineScope.launch {
                try {
                    val tempFile = File(context.cacheDir, "temp_restore.susfs_backup")
                    context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                        tempFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    // 验证备份文件
                    val backup = SuSFSManager.validateBackupFile(tempFile.absolutePath)
                    if (backup != null) {
                        selectedBackupFile = tempFile.absolutePath
                        backupInfo = backup
                        showRestoreConfirmDialog = true
                    }
                    tempFile.deleteOnExit()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                showRestoreDialog = false
            }
        }
    }

    // 加载启用功能状态
    fun loadEnabledFeatures() {
        coroutineScope.launch {
            isLoadingFeatures = true
            enabledFeatures = SuSFSManager.getEnabledFeatures(context)
            isLoadingFeatures = false
        }
    }

    // 加载应用列表
    fun loadInstalledApps() {
        coroutineScope.launch {
            installedApps = SuSFSManager.getInstalledApps()
        }
    }

    // 加载槽位信息
    fun loadSlotInfo() {
        coroutineScope.launch {
            isLoadingSlotInfo = true
            slotInfoList = SuSFSManager.getCurrentSlotInfo()
            currentActiveSlot = SuSFSManager.getCurrentActiveSlot()
            isLoadingSlotInfo = false
        }
    }

    // 加载当前配置
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val version = getSuSFSVersion()
                val binaryName = "ksu_susfs_${version.removePrefix("v")}"

                val isBinaryAvailable = try {
                    context.assets.open(binaryName).use { true }
                } catch (_: Exception) { false }

                if (!isBinaryAvailable) {
                    showVersionMismatchDialog = true
                }
            } catch (_: Exception) {
            }

            unameValue = SuSFSManager.getUnameValue(context)
            buildTimeValue = SuSFSManager.getBuildTimeValue(context)
            autoStartEnabled = SuSFSManager.isAutoStartEnabled(context)
            executeInPostFsData = SuSFSManager.getExecuteInPostFsData(context)
            susPaths = SuSFSManager.getSusPaths(context)
            susLoopPaths = SuSFSManager.getSusLoopPaths(context)
            susMaps = SuSFSManager.getSusMaps(context)
            androidDataPath = SuSFSManager.getAndroidDataPath(context)
            sdcardPath = SuSFSManager.getSdcardPath(context)
            kstatConfigs = SuSFSManager.getKstatConfigs(context)
            addKstatPaths = SuSFSManager.getAddKstatPaths(context)
            hideSusMountsForAllProcs = SuSFSManager.getHideSusMountsForAllProcs(context)
            enableHideBl = SuSFSManager.getEnableHideBl(context)
            enableCleanupResidue = SuSFSManager.getEnableCleanupResidue(context)
            enableAvcLogSpoofing = SuSFSManager.getEnableAvcLogSpoofing(context)

            loadSlotInfo()
        }
    }

    // 当切换到启用功能状态标签页时加载数据
    LaunchedEffect(selectedTab) {
        if (selectedTab == SuSFSTab.ENABLED_FEATURES) {
            loadEnabledFeatures()
        }
    }

    // 当配置变化时，自动调整开机自启动状态
    LaunchedEffect(canEnableAutoStart) {
        if (!canEnableAutoStart && autoStartEnabled) {
            autoStartEnabled = false
            SuSFSManager.configureAutoStart(context, false)
        }
    }

    // 备份对话框
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.susfs_backup_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(stringResource(R.string.susfs_backup_description))
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        val timestamp = dateFormat.format(Date())
                        backupFileLauncher.launch("SuSFS_Config_$timestamp.susfs_backup")
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.susfs_backup_create))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBackupDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    // 还原对话框
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.restore),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(stringResource(R.string.susfs_restore_description))
            },
            confirmButton = {
                Button(
                    onClick = {
                        restoreFileLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.susfs_restore_select_file))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    // 还原确认对话框
    if (showRestoreConfirmDialog && backupInfo != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                selectedBackupFile = null
                backupInfo = null
            },
            title = {
                Text(
                    text = stringResource(R.string.susfs_restore_confirm_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.susfs_restore_confirm_description))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            Text(
                                text = stringResource(R.string.susfs_backup_info_date,
                                    dateFormat.format(Date(backupInfo!!.timestamp))),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(R.string.susfs_backup_info_device, backupInfo!!.deviceInfo),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(R.string.susfs_backup_info_version, backupInfo!!.version),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedBackupFile?.let { filePath ->
                            coroutineScope.launch {
                                isLoading = true
                                val success = SuSFSManager.restoreFromBackup(context, filePath)
                                if (success) {
                                    // 重新加载所有配置
                                    unameValue = SuSFSManager.getUnameValue(context)
                                    buildTimeValue = SuSFSManager.getBuildTimeValue(context)
                                    autoStartEnabled = SuSFSManager.isAutoStartEnabled(context)
                                    executeInPostFsData = SuSFSManager.getExecuteInPostFsData(context)
                                    susPaths = SuSFSManager.getSusPaths(context)
                                    susLoopPaths = SuSFSManager.getSusLoopPaths(context)
                                    susMaps = SuSFSManager.getSusMaps(context)
                                    androidDataPath = SuSFSManager.getAndroidDataPath(context)
                                    sdcardPath = SuSFSManager.getSdcardPath(context)
                                    kstatConfigs = SuSFSManager.getKstatConfigs(context)
                                    addKstatPaths = SuSFSManager.getAddKstatPaths(context)
                                    hideSusMountsForAllProcs = SuSFSManager.getHideSusMountsForAllProcs(context)
                                    enableHideBl = SuSFSManager.getEnableHideBl(context)
                                    enableCleanupResidue = SuSFSManager.getEnableCleanupResidue(context)
                                    enableAvcLogSpoofing = SuSFSManager.getEnableAvcLogSpoofing(context)
                                }
                                isLoading = false
                                showRestoreConfirmDialog = false
                                selectedBackupFile = null
                                backupInfo = null
                            }
                        }
                    },
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.susfs_restore_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        selectedBackupFile = null
                        backupInfo = null
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    // 槽位信息对话框
    SlotInfoDialog(
        showDialog = showSlotInfoDialog,
        onDismiss = { showSlotInfoDialog = false },
        slotInfoList = slotInfoList,
        currentActiveSlot = currentActiveSlot,
        isLoadingSlotInfo = isLoadingSlotInfo,
        onRefresh = { loadSlotInfo() },
        onUseUname = { uname ->
            unameValue = uname
            showSlotInfoDialog = false
        },
        onUseBuildTime = { buildTime ->
            buildTimeValue = buildTime
            showSlotInfoDialog = false
        }
    )

    // 各种对话框
    AddPathDialog(
        showDialog = showAddPathDialog,
        onDismiss = {
            showAddPathDialog = false
            editingPath = null
        },
        onConfirm = { path ->
            coroutineScope.launch {
                isLoading = true
                val success = if (editingPath != null) {
                    SuSFSManager.editSusPath(context, editingPath!!, path)
                } else {
                    SuSFSManager.addSusPath(context, path)
                }
                if (success) {
                    susPaths = SuSFSManager.getSusPaths(context)
                }
                isLoading = false
                showAddPathDialog = false
                editingPath = null
            }
        },
        isLoading = isLoading,
        titleRes = if (editingPath != null) R.string.susfs_edit_sus_path else R.string.susfs_add_sus_path,
        labelRes = R.string.susfs_path_label,
        placeholderRes = R.string.susfs_path_placeholder,
        initialValue = editingPath ?: ""
    )

    AddPathDialog(
        showDialog = showAddLoopPathDialog,
        onDismiss = {
            showAddLoopPathDialog = false
            editingLoopPath = null
        },
        onConfirm = { path ->
            coroutineScope.launch {
                isLoading = true
                val success = if (editingLoopPath != null) {
                    SuSFSManager.editSusLoopPath(context, editingLoopPath!!, path)
                } else {
                    SuSFSManager.addSusLoopPath(context, path)
                }
                if (success) {
                    susLoopPaths = SuSFSManager.getSusLoopPaths(context)
                }
                isLoading = false
                showAddLoopPathDialog = false
                editingLoopPath = null
            }
        },
        isLoading = isLoading,
        titleRes = if (editingLoopPath != null) R.string.susfs_edit_sus_loop_path else R.string.susfs_add_sus_loop_path,
        labelRes = R.string.susfs_loop_path_label,
        placeholderRes = R.string.susfs_loop_path_placeholder,
        initialValue = editingLoopPath ?: ""
    )

    AddPathDialog(
        showDialog = showAddSusMapDialog,
        onDismiss = {
            showAddSusMapDialog = false
            editingSusMap = null
        },
        onConfirm = { path ->
            coroutineScope.launch {
                isLoading = true
                val success = if (editingSusMap != null) {
                    SuSFSManager.editSusMap(context, editingSusMap!!, path)
                } else {
                    SuSFSManager.addSusMap(context, path)
                }
                if (success) {
                    susMaps = SuSFSManager.getSusMaps(context)
                }
                isLoading = false
                showAddSusMapDialog = false
                editingSusMap = null
            }
        },
        isLoading = isLoading,
        titleRes = if (editingSusMap != null) R.string.susfs_edit_sus_map else R.string.susfs_add_sus_map,
        labelRes = R.string.susfs_sus_map_label,
        placeholderRes = R.string.susfs_sus_map_placeholder,
        initialValue = editingSusMap ?: ""
    )

    AddAppPathDialog(
        showDialog = showAddAppPathDialog,
        onDismiss = { showAddAppPathDialog = false },
        onConfirm = { packageNames ->
            coroutineScope.launch {
                isLoading = true
                var successCount = 0
                packageNames.forEach { packageName ->
                    if (SuSFSManager.addAppPaths(context, packageName)) {
                        successCount++
                    }
                }
                if (successCount > 0) {
                    susPaths = SuSFSManager.getSusPaths(context)
                }
                isLoading = false
                showAddAppPathDialog = false
            }
        },
        isLoading = isLoading,
        apps = installedApps,
        onLoadApps = { loadInstalledApps() },
        existingSusPaths = susPaths
    )

    AddKstatStaticallyDialog(
        showDialog = showAddKstatStaticallyDialog,
        onDismiss = {
            showAddKstatStaticallyDialog = false
            editingKstatConfig = null
        },
        onConfirm = { path, ino, dev, nlink, size, atime, atimeNsec, mtime, mtimeNsec, ctime, ctimeNsec, blocks, blksize ->
            coroutineScope.launch {
                isLoading = true
                val success = if (editingKstatConfig != null) {
                    SuSFSManager.editKstatConfig(
                        context,
                        editingKstatConfig!!,
                        path,
                        ino,
                        dev,
                        nlink,
                        size,
                        atime,
                        atimeNsec,
                        mtime,
                        mtimeNsec,
                        ctime,
                        ctimeNsec,
                        blocks,
                        blksize
                    )
                } else {
                    SuSFSManager.addKstatStatically(
                        context, path, ino, dev, nlink, size, atime, atimeNsec,
                        mtime, mtimeNsec, ctime, ctimeNsec, blocks, blksize
                    )
                }
                if (success) {
                    kstatConfigs = SuSFSManager.getKstatConfigs(context)
                }
                isLoading = false
                showAddKstatStaticallyDialog = false
                editingKstatConfig = null
            }
        },
        isLoading = isLoading,
        initialConfig = editingKstatConfig ?: ""
    )

    AddPathDialog(
        showDialog = showAddKstatDialog,
        onDismiss = {
            showAddKstatDialog = false
            editingKstatPath = null
        },
        onConfirm = { path ->
            coroutineScope.launch {
                isLoading = true
                val success = if (editingKstatPath != null) {
                    SuSFSManager.editAddKstat(context, editingKstatPath!!, path)
                } else {
                    SuSFSManager.addKstat(context, path)
                }
                if (success) {
                    addKstatPaths = SuSFSManager.getAddKstatPaths(context)
                }
                isLoading = false
                showAddKstatDialog = false
                editingKstatPath = null
            }
        },
        isLoading = isLoading,
        titleRes = if (editingKstatPath != null) R.string.edit_kstat_path_title else R.string.add_kstat_path_title,
        labelRes = R.string.file_or_directory_path_label,
        placeholderRes = R.string.susfs_path_placeholder,
        initialValue = editingKstatPath ?: ""
    )

    // 确认对话框
    ConfirmDialog(
        showDialog = showConfirmReset,
        onDismiss = { showConfirmReset = false },
        onConfirm = {
            showConfirmReset = false
            coroutineScope.launch {
                isLoading = true
                if (SuSFSManager.resetToDefault(context)) {
                    unameValue = "default"
                    buildTimeValue = "default"
                    autoStartEnabled = false
                }
                isLoading = false
            }
        },
        titleRes = R.string.susfs_reset_confirm_title,
        messageRes = R.string.susfs_reset_confirm_title,
        isLoading = isLoading,
        isDestructive = true
    )

    // 重置对话框
    ConfirmDialog(
        showDialog = showResetPathsDialog,
        onDismiss = { showResetPathsDialog = false },
        onConfirm = {
            coroutineScope.launch {
                isLoading = true
                SuSFSManager.saveSusPaths(context, emptySet())
                susPaths = emptySet()
                if (SuSFSManager.isAutoStartEnabled(context)) {
                    SuSFSManager.configureAutoStart(context, true)
                }
                isLoading = false
                showResetPathsDialog = false
            }
        },
        titleRes = R.string.susfs_reset_paths_title,
        messageRes = R.string.susfs_reset_paths_message,
        isLoading = isLoading,
        isDestructive = true
    )

    ConfirmDialog(
        showDialog = showResetLoopPathsDialog,
        onDismiss = { showResetLoopPathsDialog = false },
        onConfirm = {
            coroutineScope.launch {
                isLoading = true
                SuSFSManager.saveSusLoopPaths(context, emptySet())
                susLoopPaths = emptySet()
                if (SuSFSManager.isAutoStartEnabled(context)) {
                    SuSFSManager.configureAutoStart(context, true)
                }
                isLoading = false
                showResetLoopPathsDialog = false
            }
        },
        titleRes = R.string.susfs_reset_loop_paths_title,
        messageRes = R.string.susfs_reset_loop_paths_message,
        isLoading = isLoading,
        isDestructive = true
    )

    ConfirmDialog(
        showDialog = showResetSusMapsDialog,
        onDismiss = { showResetSusMapsDialog = false },
        onConfirm = {
            coroutineScope.launch {
                isLoading = true
                SuSFSManager.saveSusMaps(context, emptySet())
                susMaps = emptySet()
                if (SuSFSManager.isAutoStartEnabled(context)) {
                    SuSFSManager.configureAutoStart(context, true)
                }
                isLoading = false
                showResetSusMapsDialog = false
            }
        },
        titleRes = R.string.susfs_reset_sus_maps_title,
        messageRes = R.string.susfs_reset_sus_maps_message,
        isLoading = isLoading,
        isDestructive = true
    )

    ConfirmDialog(
        showDialog = showResetKstatDialog,
        onDismiss = { showResetKstatDialog = false },
        onConfirm = {
            coroutineScope.launch {
                isLoading = true
                SuSFSManager.saveKstatConfigs(context, emptySet())
                SuSFSManager.saveAddKstatPaths(context, emptySet())
                kstatConfigs = emptySet()
                addKstatPaths = emptySet()
                if (SuSFSManager.isAutoStartEnabled(context)) {
                    SuSFSManager.configureAutoStart(context, true)
                }
                isLoading = false
                showResetKstatDialog = false
            }
        },
        titleRes = R.string.reset_kstat_config_title,
        messageRes = R.string.reset_kstat_config_message,
        isLoading = isLoading,
        isDestructive = true
    )
    val enableBlur = LocalEnableBlur.current
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop(enableBlur)
    val barColor = if (backdrop != null) Color.Transparent else MiuixColorScheme.surface
    val navigator = LocalNavigator.current

    MiuixScaffold(
        topBar = {
            BlurredBar(backdrop) {
                MiuixTopAppBar(
                    color = barColor,
                    title = stringResource(R.string.susfs_config_title),
                    navigationIcon = {
                        MiuixIconButton(onClick = { if (!isNavigating) { isNavigating = true; navigator.pop() } }) {
                            val ld = LocalLayoutDirection.current
                            MiuixIcon(
                                modifier = Modifier.graphicsLayer { if (ld == LayoutDirection.Rtl) scaleX = -1f },
                                imageVector = MiuixIcons.Back,
                                tint = MiuixColorScheme.onSurface,
                                contentDescription = null,
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        bottomBar = {
            Surface(color = Color.Transparent) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        SuSFSTab.BASIC_SETTINGS -> {
                            // 应用按钮
                            Button(
                                onClick = {
                                    if (unameValue.isNotBlank() || buildTimeValue.isNotBlank()) {
                                        coroutineScope.launch {
                                            isLoading = true
                                            val finalUnameValue = unameValue.trim().ifBlank { "default" }
                                            val finalBuildTimeValue = buildTimeValue.trim().ifBlank { "default" }
                                            val success = SuSFSManager.setUname(context, finalUnameValue, finalBuildTimeValue)
                                            if (success) {
                                                SuSFSManager.saveExecuteInPostFsData(context, executeInPostFsData)
                                                SuSFSManager.saveEnableHideBl(context, enableHideBl)
                                                SuSFSManager.saveEnableCleanupResidue(context, enableCleanupResidue)
                                                SuSFSManager.saveEnableAvcLogSpoofing(context, enableAvcLogSpoofing)
                                            }
                                            isLoading = false
                                        }
                                    }
                                },
                                enabled = !isLoading && (unameValue.isNotBlank() || buildTimeValue.isNotBlank()),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Text(
                                    stringResource(R.string.susfs_apply),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // 重置按钮
                            OutlinedButton(
                                onClick = { showConfirmReset = true },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestoreFromTrash,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    stringResource(R.string.susfs_reset_to_default),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        SuSFSTab.SUS_PATHS -> {
                            OutlinedButton(
                                onClick = { showResetPathsDialog = true },
                                enabled = !isLoading && susPaths.isNotEmpty(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestoreFromTrash,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    stringResource(R.string.susfs_reset_paths_title),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        SuSFSTab.SUS_LOOP_PATHS -> {
                            OutlinedButton(
                                onClick = { showResetLoopPathsDialog = true },
                                enabled = !isLoading && susLoopPaths.isNotEmpty(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestoreFromTrash,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    stringResource(R.string.susfs_reset_loop_paths_title),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        SuSFSTab.SUS_MAPS -> {
                            OutlinedButton(
                                onClick = { showResetSusMapsDialog = true },
                                enabled = !isLoading && susMaps.isNotEmpty(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestoreFromTrash,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    stringResource(R.string.susfs_reset_sus_maps_title),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        SuSFSTab.KSTAT_CONFIG -> {
                            OutlinedButton(
                                onClick = { showResetKstatDialog = true },
                                enabled = !isLoading && (kstatConfigs.isNotEmpty() || addKstatPaths.isNotEmpty()),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestoreFromTrash,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    stringResource(R.string.reset_kstat_config_title),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        SuSFSTab.PATH_SETTINGS -> {
                            OutlinedButton(
                                onClick = {
                                    androidDataPath = "/sdcard/Android/data"
                                    sdcardPath = "/sdcard"
                                    coroutineScope.launch {
                                        isLoading = true
                                        SuSFSManager.setAndroidDataPath(context, androidDataPath)
                                        SuSFSManager.setSdcardPath(context, sdcardPath)
                                        isLoading = false
                                    }
                                },
                                enabled = !isLoading,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestoreFromTrash,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    stringResource(R.string.susfs_reset_path_title),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        SuSFSTab.ENABLED_FEATURES -> {
                            Button(
                                onClick = { loadEnabledFeatures() },
                                enabled = !isLoadingFeatures,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    stringResource(R.string.refresh),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        },
        popupHost = { },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Box(modifier = if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                ) {
                    allTabs.forEach { tab ->
                        val selected = selectedTab == tab
                        Surface(
                            onClick = { selectedTab = tab },
                            color = if (selected) MiuixColorScheme.primary else MiuixColorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = stringResource(tab.displayNameRes),
                                color = if (selected) MiuixColorScheme.onPrimary else MiuixColorScheme.onSurface,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    SuSFSTab.BASIC_SETTINGS -> {
                        BasicSettingsContent(
                            unameValue = unameValue,
                            onUnameValueChange = { unameValue = it },
                            buildTimeValue = buildTimeValue,
                            onBuildTimeValueChange = { buildTimeValue = it },
                            executeInPostFsData = executeInPostFsData,
                            onExecuteInPostFsDataChange = { executeInPostFsData = it },
                            autoStartEnabled = autoStartEnabled,
                            canEnableAutoStart = canEnableAutoStart,
                            isLoading = isLoading,
                            onAutoStartToggle = { enabled ->
                                if (canEnableAutoStart) {
                                    coroutineScope.launch {
                                        isLoading = true
                                        if (SuSFSManager.configureAutoStart(context, enabled)) {
                                            autoStartEnabled = enabled
                                        }
                                        isLoading = false
                                    }
                                }
                            },
                            onShowSlotInfo = { showSlotInfoDialog = true },
                            context = context,
                            onShowBackupDialog = { showBackupDialog = true },
                            onShowRestoreDialog = { showRestoreDialog = true },
                            enableHideBl = enableHideBl,
                            onEnableHideBlChange = { enabled ->
                                enableHideBl = enabled
                                SuSFSManager.saveEnableHideBl(context, enabled)
                                if (SuSFSManager.isAutoStartEnabled(context)) {
                                    coroutineScope.launch {
                                        SuSFSManager.configureAutoStart(context, true)
                                    }
                                }
                            },
                            enableCleanupResidue = enableCleanupResidue,
                            onEnableCleanupResidueChange = { enabled ->
                                enableCleanupResidue = enabled
                                SuSFSManager.saveEnableCleanupResidue(context, enabled)
                                if (SuSFSManager.isAutoStartEnabled(context)) {
                                    coroutineScope.launch {
                                        SuSFSManager.configureAutoStart(context, true)
                                    }
                                }
                            },
                            enableAvcLogSpoofing = enableAvcLogSpoofing,
                            onEnableAvcLogSpoofingChange = { enabled ->
                                coroutineScope.launch {
                                    isLoading = true
                                    val success = SuSFSManager.setEnableAvcLogSpoofing(context, enabled)
                                    if (success) {
                                        enableAvcLogSpoofing = enabled
                                    }
                                    isLoading = false
                                }
                            },
                            hideSusMountsForAllProcs = hideSusMountsForAllProcs,
                            onHideSusMountsForAllProcsChange = { hideForAll ->
                                coroutineScope.launch {
                                    isLoading = true
                                    if (SuSFSManager.setHideSusMountsForAllProcs(
                                            context,
                                            hideForAll
                                        )
                                    ) {
                                        hideSusMountsForAllProcs = hideForAll
                                    }
                                    isLoading = false
                                }
                            }
                        )
                    }
                    SuSFSTab.SUS_PATHS -> {
                        SusPathsContent(
                            susPaths = susPaths,
                            isLoading = isLoading,
                            onAddPath = { showAddPathDialog = true },
                            onAddAppPath = { showAddAppPathDialog = true },
                            onRemovePath = { path ->
                                coroutineScope.launch {
                                    isLoading = true
                                    if (SuSFSManager.removeSusPath(context, path)) {
                                        susPaths = SuSFSManager.getSusPaths(context)
                                    }
                                    isLoading = false
                                }
                            },
                            onEditPath = { path ->
                                editingPath = path
                                showAddPathDialog = true
                            },
                            forceRefreshApps = selectedTab == SuSFSTab.SUS_PATHS
                        )
                    }
                    SuSFSTab.SUS_LOOP_PATHS -> {
                        SusLoopPathsContent(
                            susLoopPaths = susLoopPaths,
                            isLoading = isLoading,
                            onAddLoopPath = { showAddLoopPathDialog = true },
                            onRemoveLoopPath = { path ->
                                coroutineScope.launch {
                                    isLoading = true
                                    if (SuSFSManager.removeSusLoopPath(context, path)) {
                                        susLoopPaths = SuSFSManager.getSusLoopPaths(context)
                                    }
                                    isLoading = false
                                }
                            },
                            onEditLoopPath = { path ->
                                editingLoopPath = path
                                showAddLoopPathDialog = true
                            }
                        )
                    }
                    SuSFSTab.SUS_MAPS -> {
                        SusMapsContent(
                            susMaps = susMaps,
                            isLoading = isLoading,
                            onAddSusMap = { showAddSusMapDialog = true },
                            onRemoveSusMap = { map ->
                                coroutineScope.launch {
                                    isLoading = true
                                    if (SuSFSManager.removeSusMap(context, map)) {
                                        susMaps = SuSFSManager.getSusMaps(context)
                                    }
                                    isLoading = false
                                }
                            },
                            onEditSusMap = { map ->
                                editingSusMap = map
                                showAddSusMapDialog = true
                            }
                        )
                    }

                    SuSFSTab.KSTAT_CONFIG -> {
                        KstatConfigContent(
                            kstatConfigs = kstatConfigs,
                            addKstatPaths = addKstatPaths,
                            isLoading = isLoading,
                            onAddKstatStatically = { showAddKstatStaticallyDialog = true },
                            onAddKstat = { showAddKstatDialog = true },
                            onRemoveKstatConfig = { config ->
                                coroutineScope.launch {
                                    isLoading = true
                                    if (SuSFSManager.removeKstatConfig(context, config)) {
                                        kstatConfigs = SuSFSManager.getKstatConfigs(context)
                                    }
                                    isLoading = false
                                }
                            },
                            onEditKstatConfig = { config ->
                                editingKstatConfig = config
                                showAddKstatStaticallyDialog = true
                            },
                            onRemoveAddKstat = { path ->
                                coroutineScope.launch {
                                    isLoading = true
                                    if (SuSFSManager.removeAddKstat(context, path)) {
                                        addKstatPaths = SuSFSManager.getAddKstatPaths(context)
                                    }
                                    isLoading = false
                                }
                            },
                            onEditAddKstat = { path ->
                                editingKstatPath = path
                                showAddKstatDialog = true
                            },
                            onUpdateKstat = { path ->
                                coroutineScope.launch {
                                    isLoading = true
                                    SuSFSManager.updateKstat(context, path)
                                    isLoading = false
                                }
                            },
                            onUpdateKstatFullClone = { path ->
                                coroutineScope.launch {
                                    isLoading = true
                                    SuSFSManager.updateKstatFullClone(context, path)
                                    isLoading = false
                                }
                            }
                        )
                    }
                    SuSFSTab.PATH_SETTINGS -> {
                        PathSettingsContent(
                            androidDataPath = androidDataPath,
                            onAndroidDataPathChange = { androidDataPath = it },
                            sdcardPath = sdcardPath,
                            onSdcardPathChange = { sdcardPath = it },
                            isLoading = isLoading,
                            onSetAndroidDataPath = {
                                coroutineScope.launch {
                                    isLoading = true
                                    SuSFSManager.setAndroidDataPath(context, androidDataPath.trim())
                                    isLoading = false
                                }
                            },
                            onSetSdcardPath = {
                                coroutineScope.launch {
                                    isLoading = true
                                    SuSFSManager.setSdcardPath(context, sdcardPath.trim())
                                    isLoading = false
                                }
                            }
                        )
                    }
                    SuSFSTab.ENABLED_FEATURES -> {
                        EnabledFeaturesContent(
                            enabledFeatures = enabledFeatures,
                            onRefresh = { loadEnabledFeatures() }
                        )
                    }
                }
                }
            }
        }
    }
}
