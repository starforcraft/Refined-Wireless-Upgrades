package com.ultramega.rsinsertexportupgrade.common.mixin;

import com.refinedmods.refinedstorage.common.support.slotreference.InventorySlotReference;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InventorySlotReference.class)
public interface InventorySlotReferenceAccessor {
    @Invoker("<init>")
    static InventorySlotReference create(final int slotIndex) {
        throw new AssertionError();
    }
}
