package com.ultramega.refinedwirelessupgrades.common.mixin;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApiProxy;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RefinedStorageApiProxy.class)
public interface RefinedStorageApiProxyInvoker {
    @Invoker(value = "ensureLoaded", remap = false)
    RefinedStorageApi wirelessUpgrades$ensureLoaded();
}
