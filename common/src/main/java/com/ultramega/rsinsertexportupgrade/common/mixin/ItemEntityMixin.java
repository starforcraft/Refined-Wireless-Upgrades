package com.ultramega.rsinsertexportupgrade.common.mixin;

import com.ultramega.rsinsertexportupgrade.common.transfer.MagnetUpgradeProcessor;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Redirect(method = "playerTouch",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"), require = 1)
    private boolean wirelessUpgrades$magnetPickup(final Inventory inventory, final ItemStack stack) {
        return MagnetUpgradeProcessor.addPickedUpStack(inventory, stack);
    }
}
