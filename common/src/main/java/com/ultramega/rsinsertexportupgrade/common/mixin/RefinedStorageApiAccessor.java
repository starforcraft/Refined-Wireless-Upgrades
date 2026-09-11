package com.ultramega.rsinsertexportupgrade.common.mixin;

import com.refinedmods.refinedstorage.common.RefinedStorageApiImpl;
import com.refinedmods.refinedstorage.common.support.slotreference.CompositeSlotReferenceProvider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RefinedStorageApiImpl.class)
public interface RefinedStorageApiAccessor {
    @Accessor(value = "slotReferenceProvider", remap = false)
    CompositeSlotReferenceProvider insertExport$getSlotReferenceProvider();
}
