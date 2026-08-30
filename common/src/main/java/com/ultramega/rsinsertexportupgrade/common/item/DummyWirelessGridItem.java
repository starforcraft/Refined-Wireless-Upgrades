package com.ultramega.rsinsertexportupgrade.common.item;

import com.refinedmods.refinedstorage.common.grid.WirelessGridItem;

import net.minecraft.world.item.ItemStack;

public final class DummyWirelessGridItem extends WirelessGridItem {
    public DummyWirelessGridItem(final boolean creative) {
        super(creative);
    }

    @Override
    public boolean isBound(final ItemStack stack) {
        return true;
    }
}
