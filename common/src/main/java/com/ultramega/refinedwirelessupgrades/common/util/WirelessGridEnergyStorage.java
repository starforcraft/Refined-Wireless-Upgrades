package com.ultramega.refinedwirelessupgrades.common.util;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.common.content.DataComponents;
import com.refinedmods.refinedstorage.common.support.energy.ItemEnergyStorage;

import net.minecraft.world.item.ItemStack;

public final class WirelessGridEnergyStorage extends ItemEnergyStorage {
    public static final int MAX_CARDS = 2;

    private final long baseCapacity;

    public WirelessGridEnergyStorage(final ItemStack stack, final long baseCapacity) {
        super(stack, new EnergyStorageImpl(baseCapacity));
        this.baseCapacity = baseCapacity;
    }

    @Override
    public long getCapacity() {
        final long bonus = WirelessGridUpgradeStorage.getEnergyCapacityBonus(this.getStack());
        return this.baseCapacity > Long.MAX_VALUE - bonus ? Long.MAX_VALUE : this.baseCapacity + bonus;
    }

    @Override
    public long getStored() {
        return Math.clamp(this.getStack().getOrDefault(DataComponents.INSTANCE.getEnergy(), 0L), 0, this.getCapacity());
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
}
