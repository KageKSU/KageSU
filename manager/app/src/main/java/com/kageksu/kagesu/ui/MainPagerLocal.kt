// From tiann/KernelSU (github.com/tiann/KernelSU) MainActivity — the main pager
// CompositionLocal that the ported Miuix bottom bar / navigation rail read. Provided
// in KageSU's MainActivity over its existing pager. GPL-3.0. See docs/ATTRIBUTION.md.
package com.kageksu.kagesu.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.kageksu.kagesu.ui.component.bottombar.MainPagerState

val LocalMainPagerState = staticCompositionLocalOf<MainPagerState> {
    error("LocalMainPagerState not provided")
}
