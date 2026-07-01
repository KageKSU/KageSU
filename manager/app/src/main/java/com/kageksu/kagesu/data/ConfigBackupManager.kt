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
import org.json.JSONArray
import org.json.JSONObject

/**
 * Gathers the current KageSU configuration into a JSON snapshot and applies a
 * previously-exported one. Uses org.json (the manager ships neither a
 * kotlinx-serialization-json nor a Gson runtime — see SuSFS BackupData for the
 * same convention). Repositories are injected to match the manual-DI style used
 * by the existing ViewModels.
 */
class ConfigBackupManager(
    private val context: Context,
    private val superUserRepository: SuperUserRepository,
    private val settingsRepository: SettingsRepository,
    private val templateRepository: TemplateRepository,
) {
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
        encode(backup)
    }

    /**
     * Apply an exported config. Returns a [RestoreReport] describing what was applied
     * vs skipped (e.g. profiles whose app is no longer installed) rather than failing
     * the whole import on a single missing package.
     */
    suspend fun import(content: String): Result<RestoreReport> = runCatching {
        val backup = decode(content)
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

    // ---- JSON encode/decode (org.json) ----

    private fun encode(b: ConfigBackup): String {
        val root = JSONObject()
        root.put("version", b.version)
        root.put("exportedAt", b.exportedAt)
        root.put("managerVersionCode", b.managerVersionCode)
        b.templates?.let { root.put("templates", it) }

        val profilesArr = JSONArray()
        for (p in b.profiles) {
            val o = JSONObject()
            o.put("name", p.name)
            o.put("allowSu", p.allowSu)
            o.put("rootUseDefault", p.rootUseDefault)
            p.rootTemplate?.let { o.put("rootTemplate", it) }
            o.put("uid", p.uid)
            o.put("gid", p.gid)
            o.put("groups", JSONArray(p.groups))
            o.put("capabilities", JSONArray(p.capabilities))
            o.put("context", p.context)
            o.put("namespace", p.namespace)
            o.put("nonRootUseDefault", p.nonRootUseDefault)
            o.put("umountModules", p.umountModules)
            o.put("rules", p.rules)
            o.put("flags", p.flags)
            profilesArr.put(o)
        }
        root.put("profiles", profilesArr)

        val s = b.settings
        val so = JSONObject()
        s.suEnabled?.let { so.put("suEnabled", it) }
        s.suCompatMode?.let { so.put("suCompatMode", it) }
        s.kernelUmountEnabled?.let { so.put("kernelUmountEnabled", it) }
        s.selinuxHideEnabled?.let { so.put("selinuxHideEnabled", it) }
        s.sulogEnabled?.let { so.put("sulogEnabled", it) }
        s.adbRootEnabled?.let { so.put("adbRootEnabled", it) }
        s.defaultUmountModules?.let { so.put("defaultUmountModules", it) }
        root.put("settings", so)

        return root.toString(2)
    }

    private fun decode(content: String): ConfigBackup {
        val root = JSONObject(content)

        val profiles = mutableListOf<BackupProfile>()
        val arr = root.optJSONArray("profiles") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            profiles.add(
                BackupProfile(
                    name = o.getString("name"),
                    allowSu = o.optBoolean("allowSu", false),
                    rootUseDefault = o.optBoolean("rootUseDefault", true),
                    rootTemplate = if (o.has("rootTemplate") && !o.isNull("rootTemplate")) o.getString("rootTemplate") else null,
                    uid = o.optInt("uid", Natives.ROOT_UID),
                    gid = o.optInt("gid", Natives.ROOT_GID),
                    groups = intList(o.optJSONArray("groups")),
                    capabilities = intList(o.optJSONArray("capabilities")),
                    context = o.optString("context", Natives.KERNEL_SU_DOMAIN),
                    namespace = o.optInt("namespace", Natives.Profile.Namespace.INHERITED.ordinal),
                    nonRootUseDefault = o.optBoolean("nonRootUseDefault", true),
                    umountModules = o.optBoolean("umountModules", true),
                    rules = o.optString("rules", ""),
                    flags = o.optLong("flags", Natives.FLAG_KSU_NO_NEW_PRIVS),
                )
            )
        }

        val so = root.optJSONObject("settings") ?: JSONObject()
        val settings = BackupSettings(
            suEnabled = if (so.has("suEnabled")) so.getBoolean("suEnabled") else null,
            suCompatMode = if (so.has("suCompatMode")) so.getInt("suCompatMode") else null,
            kernelUmountEnabled = if (so.has("kernelUmountEnabled")) so.getBoolean("kernelUmountEnabled") else null,
            selinuxHideEnabled = if (so.has("selinuxHideEnabled")) so.getBoolean("selinuxHideEnabled") else null,
            sulogEnabled = if (so.has("sulogEnabled")) so.getBoolean("sulogEnabled") else null,
            adbRootEnabled = if (so.has("adbRootEnabled")) so.getBoolean("adbRootEnabled") else null,
            defaultUmountModules = if (so.has("defaultUmountModules")) so.getBoolean("defaultUmountModules") else null,
        )

        return ConfigBackup(
            version = root.optInt("version", CONFIG_BACKUP_VERSION),
            exportedAt = root.optLong("exportedAt", 0L),
            managerVersionCode = root.optLong("managerVersionCode", 0L),
            profiles = profiles,
            templates = if (root.has("templates") && !root.isNull("templates")) root.getString("templates") else null,
            settings = settings,
        )
    }

    private fun intList(arr: JSONArray?): List<Int> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { arr.getInt(it) }
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
