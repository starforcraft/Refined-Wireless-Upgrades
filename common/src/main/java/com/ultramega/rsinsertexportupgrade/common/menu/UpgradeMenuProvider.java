package com.ultramega.rsinsertexportupgrade.common.menu;

import com.ultramega.rsinsertexportupgrade.common.util.UpgradeType;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;

public record UpgradeMenuProvider(UpgradeType type, SlotReference gridSlotReference, int sourceUpgradeSlot) implements MenuProvider {
    @Override
    public Component getDisplayName() {
        return createInsertExportTranslation("item", this.type.getName() + "_upgrade");
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory playerInventory, final Player player) {
        return UpgradeContainerMenu.server(this.type, syncId, playerInventory, this.gridSlotReference, this.sourceUpgradeSlot);
    }
}
