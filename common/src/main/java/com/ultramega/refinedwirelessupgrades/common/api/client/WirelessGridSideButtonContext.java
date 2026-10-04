package com.ultramega.refinedwirelessupgrades.common.api.client;

import com.ultramega.refinedwirelessupgrades.common.util.GridSlotReferenceAccessor;

import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/** Client-only context belonging to one screen */
@API(status = Status.STABLE)
public record WirelessGridSideButtonContext(AbstractGridScreen<?> screen, Inventory inventory, Item upgrade) {
    /** First installed copy, or an empty stack. The returned stack is a defensive snapshot */
    public ItemStack getUpgradeStack() {
        final Slot slot = this.findSlot();
        return slot == null ? ItemStack.EMPTY : slot.getItem().copy();
    }

    /** Index inside the grid's upgrade container, not the screen menu's slot list; -1 if absent */
    public int getUpgradeSlot() {
        final Slot slot = this.findSlot();
        return slot == null ? -1 : slot.getContainerSlot();
    }

    @Nullable
    public SlotReference getGridSlotReference() {
        return this.screen.getMenu() instanceof GridSlotReferenceAccessor accessor ? accessor.wirelessUpgrades$getGridSlotReference() : null;
    }

    @Nullable
    private Slot findSlot() {
        return this.screen.getMenu().slots.stream()
            .filter(UpgradeSlot.class::isInstance)
            .filter(slot -> slot.getItem().is(this.upgrade))
            .findFirst().orElse(null);
    }
}
