package com.ultramega.refinedwirelessupgrades.common.mixin;

import com.refinedmods.refinedstorage.common.RefinedStorageApiImpl;
import com.refinedmods.refinedstorage.common.support.slotreference.CompositePlayerSlotReferenceProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RefinedStorageApiImpl.class)
public interface RefinedStorageApiAccessor {
    @Accessor(value = "playerSlotReferenceProvider", remap = false)
    CompositePlayerSlotReferenceProvider wirelessUpgrades$getSlotReferenceProvider();
}
