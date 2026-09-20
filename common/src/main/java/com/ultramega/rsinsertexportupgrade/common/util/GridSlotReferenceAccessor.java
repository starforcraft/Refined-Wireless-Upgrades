package com.ultramega.rsinsertexportupgrade.common.util;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;

import javax.annotation.Nullable;

public interface GridSlotReferenceAccessor {
    int wirelessUpgrades$getUpgradeSlotCount();

    @Nullable
    SlotReference wirelessUpgrades$getGridSlotReference();
}
