package com.ultramega.refinedwirelessupgrades.common.mixin;

import com.ultramega.refinedwirelessupgrades.common.transfer.MagnetUpgradeProcessor;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @WrapOperation(method = "playerTouch",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"), require = 1)
    private boolean wirelessUpgrades$magnetPickup(final Inventory inventory, final ItemStack stack, final Operation<Boolean> original) {
        return MagnetUpgradeProcessor.addPickedUpStack(inventory, stack, original::call);
    }
}
