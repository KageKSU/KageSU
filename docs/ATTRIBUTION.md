# Attribution

KageSU's Miuix UI is **not original work**. It was ported and adapted from existing
GPL-3.0 / Apache-2.0 projects. This file records the original authorship and sources so
that credit and copyright are preserved, as required by the licenses.

## Miuix UI screens & components — © YuKongA, from SukiSU-Ultra (GPL-3.0)

The Miuix design, screens and Miuix-specific components below were **originally authored by
[YuKongA](https://github.com/YuKongA)** (author of the [miuix](https://github.com/miuix-kotlin-multiplatform/miuix)
library) for **[SukiSU-Ultra](https://github.com/SukiSU-Ultra/SukiSU-Ultra)**, licensed under
**GPL-3.0**. The original author retains copyright. KageSU only renamed the package
(`com.sukisu.ultra` → `com.kageksu.kagesu`), wired them to ReSukiSU's viewmodels/navigation,
and added ReSukiSU-specific features.

- `ui/theme/MiuixTheme.kt`, `ui/theme/UiMode.kt`
- `ui/component/bottombar/BottomBarMiuix.kt`, `NavigationRailMiuix.kt`
- `ui/component/miuix/WarningCard.kt`, `DropdownItem.kt`, `StatusTag.kt`
- `ui/component/rebootlistpopup/RebootListPopupMiuix.kt`
- `ui/screen/main/HomeMiuix.kt`, `SuperUserMiuix.kt`, `SettingsMiuix.kt`, `ModuleMiuix.kt`
- `ui/screen/about/AboutMiuix.kt`
- `ui/screen/UmountManagerMiuix.kt`, `DynamicManagerMiuix.kt`, `ExecuteModuleActionMiuix.kt`
- `ui/screen/moduleRepo/ModuleRepoMiuix.kt`

(The KageSU-main branch these were also mirrored through is itself a SukiSU-lineage fork; the
root author of the Miuix code is YuKongA.)

## Liquid-glass & background effects — Apache-2.0

- `ui/component/FloatingBottomBar.kt`, `ui/component/miuix/effect/*` — based on the
  [compose-miuix-ui](https://github.com/miuix-kotlin-multiplatform/miuix) examples (© YuKongA, Apache-2.0).
- `ui/component/liquid/*` — adapted from
  [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) (Apache-2.0).

## Note on git history

These files were brought in by copy-and-adapt, so this project's own git history does not
carry the original authors' commit authorship. This `ATTRIBUTION.md`, the per-file headers,
and the README credit are provided to preserve authorship and copyright. If you reuse this
code, keep these notices intact (GPL-3.0 requires it).
