plugins {
    alias(libs.plugins.agp.app) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.compose.compiler) apply false
}

val androidMinSdkVersion by extra(26)
val androidTargetSdkVersion by extra(37)
val androidCompileSdkVersion by extra(37)
val androidBuildToolsVersion by extra("36.1.0")
val androidCompileNdkVersion by extra(libs.versions.ndk.get())
val androidSourceCompatibility by extra(JavaVersion.VERSION_21)
val androidTargetCompatibility by extra(JavaVersion.VERSION_21)
val managerVersionCode by extra(getVersionCode())
val managerVersionName by extra(getGitDescribe())

fun getGitCommitCount(): Int {
    return providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
    }.standardOutput.asText.get().trim().toInt()
}

fun getGitDescribe(): String {
    return providers.exec {
        commandLine("git", "describe", "--tags", "--always", "--abbrev=0")
    }.standardOutput.asText.get().trim()
}

// KageSU version scheme, continuing the ReSukiSU (v4.x) lineage this branch is
// based on. major*10000 keeps the code in the 4_xxxx range (above ReSukiSU's
// ~35000 so upgrades apply); BASELINE is tuned so the v4.1.1 tag lands at 4_1000
// and the code grows by one per commit thereafter.
fun getVersionCode(): Int {
    val major = 4
    val baseline = 3310
    return major * 10000 + getGitCommitCount() - baseline
}
