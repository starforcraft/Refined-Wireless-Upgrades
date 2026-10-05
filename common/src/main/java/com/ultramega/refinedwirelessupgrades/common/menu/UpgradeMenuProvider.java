package com.ultramega.refinedwirelessupgrades.common.menu;

import com.ultramega.refinedwirelessupgrades.common.util.UpgradeType;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;

public record UpgradeMenuProvider(UpgradeType type, PlayerSlotReference gridSlotReference, int sourceUpgradeSlot) implements MenuProvider {
    @Override
    public Component getDisplayName() {
        return createInsertExportTranslation("item", this.type.getName() + "_upgrade");
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory playerInventory, final Player player) {
        if (this.type == UpgradeType.MAGNET) {
            return MagnetContainerMenu.server(syncId, playerInventory, this.gridSlotReference, this.sourceUpgradeSlot);
        }
        return UpgradeContainerMenu.server(this.type, syncId, playerInventory, this.gridSlotReference, this.sourceUpgradeSlot);
    }
}
