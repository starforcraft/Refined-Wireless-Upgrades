package com.ultramega.refinedwirelessupgrades.common.util;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;
import com.refinedmods.refinedstorage.common.content.DataComponents;

import net.minecraft.world.item.ItemStack;

public final class WirelessGridEnergyStorage implements EnergyStorage {
    public static final int MAX_CARDS = 2;

    private final long baseCapacity;
    private final EnergyItemContext context;
    private ItemStack stack;

    public WirelessGridEnergyStorage(final ItemStack stack, final long baseCapacity, final EnergyItemContext context) {
        this.stack = stack.copy();
        this.context = context;
        this.baseCapacity = baseCapacity;
    }

    @Override
    public long getCapacity() {
        final long bonus = WirelessGridUpgradeStorage.getEnergyCapacityBonus(this.stack);
        return this.baseCapacity > Long.MAX_VALUE - bonus ? Long.MAX_VALUE : this.baseCapacity + bonus;
    }

    @Override
    public long getStored() {
        return Math.clamp(this.stack.getOrDefault(DataComponents.INSTANCE.getEnergy(), 0L), 0, this.getCapacity());
    }

    @Override
    public long receive(final long amount, final Action action) {
        final long stored = this.getStored();
        final long received = Math.clamp(amount, 0, this.getCapacity() - stored);
        if (received > 0 && action == Action.EXECUTE) {
            this.onStoredChanged(stored + received);
        }
        return received;
    }

    @Override
    public long extract(final long amount, final Action action) {
        final long stored = this.getStored();
        final long extracted = Math.clamp(amount, 0, stored);
        if (extracted > 0 && action == Action.EXECUTE) {
            this.onStoredChanged(stored - extracted);
        }
        return extracted;
    }

    private void onStoredChanged(final long stored) {
        final ItemStack current = this.context.copyStack();
        this.stack = current.isEmpty() ? this.stack.copy() : current;
        this.stack.set(DataComponents.INSTANCE.getEnergy(), stored);
        this.context.setStack(this.stack.copy());
    }
}
