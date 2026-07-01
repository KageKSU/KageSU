package com.kageksu.kagesu.data.model

import com.kageksu.kagesu.Natives
import kotlinx.serialization.Serializable

/**
 * On-disk schema version for the exported config backup. Bump when the shape of
 * [ConfigBackup] changes in a backwards-incompatible way; [ConfigBackup.version]
 * is checked on import so older/newer files are rejected cleanly instead of
 * crashing or silently applying garbage.
 */
const val CONFIG_BACKUP_VERSION = 1

/**
 * A portable snapshot of the user's KageSU configuration: per-app root profiles,
 * the app-profile templates, and the kernel/manager feature toggles. Intended to
 * survive a kernel reflash or a move to a new device. Module zips and the module
 * repo cache are deliberately NOT included (too large / device-specific); only the
 * enabled-state of modules is captured.
 */
@Serializable
data class ConfigBackup(
    val version: Int = CONFIG_BACKUP_VERSION,
    val exportedAt: Long = 0L,
    val managerVersionCode: Long = 0L,
    val profiles: List<BackupProfile> = emptyList(),
    /** Raw JSON from [com.kageksu.kagesu.data.repository.TemplateRepository.exportTemplates]. */
    val templates: String? = null,
    val settings: BackupSettings = BackupSettings(),
    val modules: List<BackupModuleState> = emptyList(),
)

/** Serializable mirror of [Natives.Profile] (which is Parcelable, not Serializable). */
@Serializable
data class BackupProfile(
    val name: String,
    val allowSu: Boolean = false,
    val rootUseDefault: Boolean = true,
    val rootTemplate: String? = null,
    val uid: Int = Natives.ROOT_UID,
    val gid: Int = Natives.ROOT_GID,
    val groups: List<Int> = emptyList(),
    val capabilities: List<Int> = emptyList(),
    val context: String = Natives.KERNEL_SU_DOMAIN,
    val namespace: Int = Natives.Profile.Namespace.INHERITED.ordinal,
    val nonRootUseDefault: Boolean = true,
    val umountModules: Boolean = true,
    val rules: String = "",
    val flags: Long = Natives.FLAG_KSU_NO_NEW_PRIVS,
) {
    /**
     * Rebuild a [Natives.Profile] for applying on this device. [currentUid] must be
     * the package's *current* uid here (it can differ from the exporting device), so
     * the kernel's uid<->package check stays valid.
     */
    fun toProfile(currentUid: Int): Natives.Profile = Natives.Profile(
        name = name,
        currentUid = currentUid,
        allowSu = allowSu,
        rootUseDefault = rootUseDefault,
        rootTemplate = rootTemplate,
        uid = uid,
        gid = gid,
        groups = groups,
        capabilities = capabilities,
        context = context,
        namespace = namespace,
        nonRootUseDefault = nonRootUseDefault,
        umountModules = umountModules,
        rules = rules,
        flags = flags,
    )

    companion object {
        fun from(p: Natives.Profile): BackupProfile = BackupProfile(
            name = p.name,
            allowSu = p.allowSu,
            rootUseDefault = p.rootUseDefault,
            rootTemplate = p.rootTemplate,
            uid = p.uid,
            gid = p.gid,
            groups = p.groups,
            capabilities = p.capabilities,
            context = p.context,
            namespace = p.namespace,
            nonRootUseDefault = p.nonRootUseDefault,
            umountModules = p.umountModules,
            rules = p.rules,
            flags = p.flags,
        )
    }
}

/**
 * Kernel/manager feature toggles. Nullable so an older/partial backup only restores
 * the fields it actually carries (a null field is left untouched on import).
 */
@Serializable
data class BackupSettings(
    val suEnabled: Boolean? = null,
    val suCompatMode: Int? = null,
    val kernelUmountEnabled: Boolean? = null,
    val selinuxHideEnabled: Boolean? = null,
    val sulogEnabled: Boolean? = null,
    val adbRootEnabled: Boolean? = null,
    val defaultUmountModules: Boolean? = null,
)

/** Enabled-state of an installed module, keyed by its module id. */
@Serializable
data class BackupModuleState(
    val id: String,
    val enabled: Boolean,
)
