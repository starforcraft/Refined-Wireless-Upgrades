package com.ultramega.rsinsertexportupgrade.common.mixin;

import com.ultramega.rsinsertexportupgrade.common.util.WirelessGridEnergyStorage;
import com.ultramega.rsinsertexportupgrade.common.util.WirelessGridUpgradeStorage;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.RefinedStorageApiImpl;
import com.refinedmods.refinedstorage.common.support.energy.CreativeEnergyStorage;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RefinedStorageApiImpl.class)
public abstract class RefinedStorageApiEnergyMixin {
    @Inject(method = "asItemEnergyStorage", at = @At("HEAD"), cancellable = true, remap = false)
    private void wirelessUpgrades$addCapacity(final EnergyStorage energyStorage,
                                              final ItemStack stack,
                                              final CallbackInfoReturnable<EnergyStorage> cir) {
        if (WirelessGridUpgradeStorage.isSupportedWirelessGrid(stack) && !(energyStorage instanceof CreativeEnergyStorage)) {
            cir.setReturnValue(new WirelessGridEnergyStorage(stack, energyStorage.getCapacity()));
        }
    }
}
