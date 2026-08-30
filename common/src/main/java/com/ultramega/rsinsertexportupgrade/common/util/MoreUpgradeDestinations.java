package com.ultramega.rsinsertexportupgrade.common.util;

import com.ultramega.rsinsertexportupgrade.common.registry.Items;

import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.DataComponents;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public enum MoreUpgradeDestinations implements UpgradeDestination { //TODO: separate into all variants?
    WIRELESS_GRIDS(ContentNames.WIRELESS_GRID, MoreUpgradeDestinations::createBoundCreativeWirelessGrid),
    EXPORT_UPGRADE(com.ultramega.rsinsertexportupgrade.common.registry.ContentNames.EXPORT_UPGRADE, () -> Items.INSTANCE.getExportUpgrade().getDefaultInstance());

    private final Component name;
    private final Supplier<ItemStack> stackFactory;
    @Nullable
    private ItemStack cachedStack;

    MoreUpgradeDestinations(final Component name, final Supplier<ItemStack> stackFactory) {
        this.name = name;
        this.stackFactory = stackFactory;
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public ItemStack getStackRepresentation() {
        if (this.cachedStack == null) {
            this.cachedStack = this.stackFactory.get();
        }
        return this.cachedStack;
    }

    private static ItemStack createBoundCreativeWirelessGrid() {
        final ItemStack stack = com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getCreativeWirelessGrid().getDefaultInstance();
        stack.set(DataComponents.INSTANCE.getNetworkLocation(), GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO));
        return stack;
    }
}
