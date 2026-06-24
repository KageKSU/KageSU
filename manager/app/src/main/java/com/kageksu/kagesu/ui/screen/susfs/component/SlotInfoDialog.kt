package com.kageksu.kagesu.ui.screen.susfs.component

import androidx.compose.runtime.Composable
import com.kageksu.kagesu.ui.LocalUiMode
import com.kageksu.kagesu.ui.UiMode
import com.kageksu.kagesu.ui.screen.susfs.component.miuix.SlotInfoDialogMiuix
import com.kageksu.kagesu.ui.screen.susfs.component.material.SlotInfoDialogMaterial
import com.kageksu.kagesu.ui.screen.susfs.util.SuSFSManager

@Composable
fun SlotInfoDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    slotInfoList: List<SuSFSManager.SlotInfo>,
    currentActiveSlot: String,
    isLoadingSlotInfo: Boolean,
    onRefresh: () -> Unit,
    onUseUname: (String) -> Unit,
    onUseBuildTime: (String) -> Unit
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SlotInfoDialogMiuix(
            showDialog = showDialog,
            onDismiss = onDismiss,
            slotInfoList = slotInfoList,
            currentActiveSlot = currentActiveSlot,
            isLoadingSlotInfo = isLoadingSlotInfo,
            onRefresh = onRefresh,
            onUseUname = onUseUname,
            onUseBuildTime = onUseBuildTime
        )
        UiMode.Material -> SlotInfoDialogMaterial(
            showDialog = showDialog,
            onDismiss = onDismiss,
            slotInfoList = slotInfoList,
            currentActiveSlot = currentActiveSlot,
            isLoadingSlotInfo = isLoadingSlotInfo,
            onRefresh = onRefresh,
            onUseUname = onUseUname,
            onUseBuildTime = onUseBuildTime
        )
    }
}
