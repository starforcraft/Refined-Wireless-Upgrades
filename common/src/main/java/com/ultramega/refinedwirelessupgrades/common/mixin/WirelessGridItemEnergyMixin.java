package com.ultramega.refinedwirelessupgrades.common.mixin;

import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridEnergyStorage;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;
import com.refinedmods.refinedstorage.common.grid.WirelessGridItem;
import com.refinedmods.refinedstorage.common.support.energy.CreativeEnergyStorage;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WirelessGridItem.class)
public abstract class WirelessGridItemEnergyMixin {
    @Inject(method = "createEnergyStorage", at = @At("RETURN"), cancellable = true, remap = false)
    private static void wirelessUpgrades$addCapacity(final ItemStack stack, final EnergyItemContext context,
                                                     final CallbackInfoReturnable<EnergyStorage> cir) {
        final EnergyStorage storage = cir.getReturnValue();
        if (!(storage instanceof CreativeEnergyStorage)) {
            cir.setReturnValue(new WirelessGridEnergyStorage(stack, storage.getCapacity(), context));
        }
    }
}
