package com.kageksu.kagesu.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kageksu.kagesu.R
import com.kageksu.kagesu.data.ConfigBackupManager
import com.kageksu.kagesu.data.repository.SettingsRepositoryImpl
import com.kageksu.kagesu.data.repository.SuperUserRepositoryImpl
import com.kageksu.kagesu.data.repository.TemplateRepositoryImpl
import com.kageksu.kagesu.ksuApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BackupRestoreUiState(
    val isBusy: Boolean = false,
    val message: String? = null,
)

class BackupRestoreViewModel : ViewModel() {
    private val manager = ConfigBackupManager(
        context = ksuApp,
        superUserRepository = SuperUserRepositoryImpl(),
        settingsRepository = SettingsRepositoryImpl(),
        templateRepository = TemplateRepositoryImpl(),
    )

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    fun suggestedFileName(): String = manager.suggestedFileName()

    fun export(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = null) }
            val message = withContext(Dispatchers.IO) {
                runCatching {
                    val content = manager.export().getOrThrow()
                    ksuApp.contentResolver.openOutputStream(uri)?.use {
                        it.write(content.toByteArray())
                    } ?: error("output stream unavailable")
                }.fold(
                    onSuccess = { ksuApp.getString(R.string.config_backup_export_success) },
                    onFailure = { ksuApp.getString(R.string.config_backup_export_failed, it.message ?: "") },
                )
            }
            _uiState.update { it.copy(isBusy = false, message = message) }
        }
    }

    fun import(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, message = null) }
            val message = withContext(Dispatchers.IO) {
                runCatching {
                    val content = ksuApp.contentResolver.openInputStream(uri)?.use {
                        it.readBytes().decodeToString()
                    } ?: error("input stream unavailable")
                    manager.import(content).getOrThrow()
                }.fold(
                    onSuccess = {
                        ksuApp.getString(
                            R.string.config_backup_import_success,
                            it.appliedProfiles,
                            it.skippedProfiles,
                        )
                    },
                    onFailure = { ksuApp.getString(R.string.config_backup_import_failed, it.message ?: "") },
                )
            }
            _uiState.update { it.copy(isBusy = false, message = message) }
        }
    }

    fun consumeMessage() = _uiState.update { it.copy(message = null) }
}
