package com.kageksu.kagesu.data

import android.content.Context
import android.content.pm.PackageManager
import com.kageksu.kagesu.Natives
import com.kageksu.kagesu.data.model.BackupProfile
import com.kageksu.kagesu.data.model.BackupSettings
import com.kageksu.kagesu.data.model.CONFIG_BACKUP_VERSION
import com.kageksu.kagesu.data.model.ConfigBackup
import com.kageksu.kagesu.data.repository.SettingsRepository
import com.kageksu.kagesu.data.repository.SuperUserRepository
import com.kageksu.kagesu.data.repository.TemplateRepository
import kotlinx.serialization.json.Json

/**
 * Gathers the current KageSU configuration into a [ConfigBackup] and applies a
 * previously-exported one. Pure logic; the ViewModel owns coroutine scope, file IO
 * (SAF) and user feedback. Repositories are injected so this stays testable and
 * matches the manual-DI style used by the existing ViewModels.
 */
class ConfigBackupManager(
    private val context: Context,
    private val superUserRepository: SuperUserRepository,
    private val settingsRepository: SettingsRepository,
    private val templateRepository: TemplateRepository,
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Build a snapshot of the current config. */
    suspend fun export(): Result<String> = runCatching {
        val apps = superUserRepository.getAppList().getOrThrow().first

        // Only back up apps that actually carry a custom root profile — defaults
        // are reconstructed by the kernel, so persisting them is noise.
        val profiles = apps.mapNotNull { app ->
            val profile = app.profile ?: return@mapNotNull null
            if (!app.allowSu && !app.hasCustomProfile) return@mapNotNull null
            BackupProfile.from(profile)
        }

        val backup = ConfigBackup(
            version = CONFIG_BACKUP_VERSION,
            exportedAt = System.currentTimeMillis(),
            managerVersionCode = managerVersionCode(),
            profiles = profiles,
            templates = templateRepository.exportTemplates().getOrNull(),
            settings = gatherSettings(),
        )
        json.encodeToString(ConfigBackup.serializer(), backup)
    }

    /**
     * Apply an exported config. Returns a [RestoreReport] describing what was applied
     * vs skipped (e.g. profiles whose app is no longer installed) rather than failing
     * the whole import on a single missing package.
     */
    suspend fun import(content: String): Result<RestoreReport> = runCatching {
        val backup = json.decodeFromString(ConfigBackup.serializer(), content)
        require(backup.version <= CONFIG_BACKUP_VERSION) {
            "Backup was made by a newer KageSU (schema v${backup.version}); update the app first."
        }

        var appliedProfiles = 0
        var skippedProfiles = 0
        val pm = context.packageManager
        for (bp in backup.profiles) {
            val currentUid = currentUidFor(pm, bp.name)
            if (currentUid == null) {
                // app not installed on this device — skip, don't fail the import
                skippedProfiles++
                continue
            }
            if (Natives.setAppProfile(bp.toProfile(currentUid))) appliedProfiles++ else skippedProfiles++
        }

        backup.templates?.let { templateRepository.importTemplates(it) }
        applySettings(backup.settings)

        RestoreReport(
            appliedProfiles = appliedProfiles,
            skippedProfiles = skippedProfiles,
            templatesRestored = backup.templates != null,
        )
    }

    private fun gatherSettings(): BackupSettings = BackupSettings(
        suEnabled = settingsRepository.isSuEnabled(),
        suCompatMode = settingsRepository.getSuCompatModePref(),
        kernelUmountEnabled = settingsRepository.isKernelUmountEnabled(),
        selinuxHideEnabled = settingsRepository.isSelinuxHideEnabled(),
        sulogEnabled = null, // sulog has no stable getter on this device; left for the toggle
        adbRootEnabled = null,
        defaultUmountModules = settingsRepository.isDefaultUmountModules(),
    )

    private fun applySettings(s: BackupSettings) {
        s.suEnabled?.let { settingsRepository.setSuEnabled(it) }
        s.suCompatMode?.let { settingsRepository.setSuCompatModePref(it) }
        s.kernelUmountEnabled?.let { settingsRepository.setKernelUmountEnabled(it) }
        s.selinuxHideEnabled?.let { settingsRepository.setSelinuxHideEnabled(it) }
        s.sulogEnabled?.let { settingsRepository.setSulogEnabled(it) }
        s.adbRootEnabled?.let { settingsRepository.setAdbRootEnabled(it) }
        s.defaultUmountModules?.let { settingsRepository.setDefaultUmountModules(it) }
        settingsRepository.execKsudFeatureSave()
    }

    private fun currentUidFor(pm: PackageManager, packageName: String): Int? = try {
        pm.getPackageInfo(packageName, 0).applicationInfo?.uid
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    private fun managerVersionCode(): Long = try {
        val pkg = context.packageManager.getPackageInfo(context.packageName, 0)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) pkg.longVersionCode
        else @Suppress("DEPRECATION") pkg.versionCode.toLong()
    } catch (_: Exception) {
        0L
    }

    /** Suggested export filename, e.g. `KageSU-config-20260701-1530.json`. */
    fun suggestedFileName(): String {
        val ts = android.text.format.DateFormat.format("yyyyMMdd-HHmm", System.currentTimeMillis())
        return "KageSU-config-$ts.json"
    }
}

data class RestoreReport(
    val appliedProfiles: Int,
    val skippedProfiles: Int,
    val templatesRestored: Boolean,
)
