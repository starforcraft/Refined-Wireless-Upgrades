package com.ultramega.refinedwirelessupgrades.common.util;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import org.jspecify.annotations.Nullable;

public interface GridSlotReferenceAccessor {
    int wirelessUpgrades$getUpgradeSlotCount();

    @Nullable
    PlayerSlotReference wirelessUpgrades$getGridSlotReference();
}
