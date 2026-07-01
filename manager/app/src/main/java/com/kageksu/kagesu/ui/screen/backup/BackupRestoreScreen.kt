package com.kageksu.kagesu.ui.screen.backup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kageksu.kagesu.ui.LocalUiMode
import com.kageksu.kagesu.ui.UiMode
import com.kageksu.kagesu.ui.viewmodel.BackupRestoreViewModel

@Composable
fun BackupRestoreScreen() {
    val context = LocalContext.current
    val viewModel = viewModel<BackupRestoreViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::export) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::import) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.consumeMessage()
        }
    }

    val onExport = { exportLauncher.launch(viewModel.suggestedFileName()) }
    val onImport = { importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*")) }

    when (LocalUiMode.current) {
        UiMode.Miuix -> BackupRestoreMiuix(
            isBusy = uiState.isBusy,
            onExport = onExport,
            onImport = onImport,
        )
        UiMode.Material -> BackupRestoreMaterial(
            isBusy = uiState.isBusy,
            onExport = onExport,
            onImport = onImport,
        )
    }
}
