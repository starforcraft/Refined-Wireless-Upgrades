package com.ultramega.rsinsertexportupgrade.common.mixin;

import com.ultramega.rsinsertexportupgrade.common.transfer.UpgradeProcessor;
import com.ultramega.rsinsertexportupgrade.common.util.WirelessGridUpgradeStorage;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.energy.AbstractNetworkEnergyItem;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractNetworkEnergyItem.class)
public abstract class AbstractNetworkEnergyItemMixin extends Item {
    protected AbstractNetworkEnergyItemMixin(final Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(final ItemStack stack, final Level level, final Entity entity, final int slotId, final boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide
            || !(entity instanceof ServerPlayer player)
            || !WirelessGridUpgradeStorage.isSupportedWirelessGrid(stack)
            || !WirelessGridUpgradeStorage.hasUpgrades(stack)
            || slotId < 0
            || slotId >= player.getInventory().getContainerSize()) {
            return;
        }

        final SlotReference slotReference = InventorySlotReferenceAccessor.create(slotId);
        if (slotReference.resolve(player).orElse(ItemStack.EMPTY) != stack) {
            return;
        }

        final NetworkItemContext context = RefinedStorageApi.INSTANCE.getNetworkItemHelper().createContext(stack, player, slotReference);
        UpgradeProcessor.tick(stack, player, slotId, context);
    }
}
