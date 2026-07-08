# Attribution

The Miuix UI on this branch is **not original work**. It was merged and adapted from
[tiann/KernelSU](https://github.com/tiann/KernelSU) (GPL-3.0). This file records the
original authorship and sources so that credit and copyright are preserved, as the
license requires.

## Miuix UI — from tiann/KernelSU (GPL-3.0)

The Miuix design, screens, theme and Miuix-specific components were **originally authored
for [tiann/KernelSU](https://github.com/tiann/KernelSU)**, principally by
[YuKongA](https://github.com/YuKongA) (author of the
[miuix](https://github.com/miuix-kotlin-multiplatform/miuix) library), with contributions
from KOWX712, weishu and other KernelSU contributors. Licensed under **GPL-3.0**; the
original authors retain copyright.

KageSU merged tiann/KernelSU's history into the `upstream` branch (preserving the upstream
commit authorship), then adapted the Miuix layer to KageSU/ReSukiSU's tree:

- renamed the package (`me.weishu.kernelsu` → `com.kageksu.kagesu`),
- wired the Miuix screens to KageSU/ReSukiSU's viewmodels and navigation via small
  bridge/compat files, and
- kept KageSU's own Material UI alongside (both UI modes coexist).

Ported/adapted areas:

- `ui/UiMode.kt`, `ui/theme/MiuixTheme.kt`, `ui/theme/MiuixThemeCompat.kt`
- `ui/component/miuix/*`, `ui/component/bottombar/*`, `ui/component/liquid/*`,
  `ui/component/profile/*`, `ui/component/material/SegmentedList.kt`
- `ui/screen/*/‌*Miuix.kt` and their `*UiState.kt` (Home, SuperUser, Module, Settings,
  About, Install, Flash, Template, TemplateEditor, Sulog, ModuleRepo, ExecuteModuleAction,
  AppProfile)
- `data/model/*`, `data/repository/*` (tiann's data layer, rebranded)

## Liquid-glass & background effects — Apache-2.0

- `ui/component/liquid/*`, `ui/component/miuix/effect/*` — based on the
  [compose-miuix-ui](https://github.com/miuix-kotlin-multiplatform/miuix) examples
  (© YuKongA, Apache-2.0) and
  [Kyant0/AndroidLiquidGlass](https://github.com/Kyant0/AndroidLiquidGlass) (Apache-2.0).

## Note on git history

The Miuix code was brought in by **merging tiann/KernelSU** (so upstream's commit history
and authorship are preserved in this branch's ancestry) and then adapted. This
`ATTRIBUTION.md`, the per-file headers, and the README credit further preserve authorship
and copyright. If you reuse this code, keep these notices intact (GPL-3.0 requires it).
