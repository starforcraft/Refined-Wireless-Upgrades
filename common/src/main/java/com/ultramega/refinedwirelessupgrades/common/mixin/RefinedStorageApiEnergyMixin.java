package com.ultramega.refinedwirelessupgrades.common.mixin;

import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridEnergyStorage;
import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridUpgradeStorage;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.RefinedStorageApiImpl;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;
import com.refinedmods.refinedstorage.common.support.energy.CreativeEnergyStorage;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RefinedStorageApiImpl.class)
public abstract class RefinedStorageApiEnergyMixin {
    @Inject(method = "createItemEnergyStorage", at = @At("HEAD"), cancellable = true, remap = false)
    private void wirelessUpgrades$addCapacity(final EnergyStorage energyStorage,
                                              final ItemStack stack,
                                              final EnergyItemContext context,
                                              final CallbackInfoReturnable<EnergyStorage> cir) {
        if (WirelessGridUpgradeStorage.isSupportedWirelessGrid(stack) && !(energyStorage instanceof CreativeEnergyStorage)) {
            cir.setReturnValue(new WirelessGridEnergyStorage(stack, energyStorage.getCapacity(), context));
        }
    }
}
