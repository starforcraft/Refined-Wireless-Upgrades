package com.ultramega.rsinsertexportupgrade.common.util;

import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.MOD_ID;

public final class WirelessGridUpgradeStorage {
    public static final int SLOT_COUNT = 2;

    private static final String UPGRADES_TAG = "Upgrades";

    private WirelessGridUpgradeStorage() {
    }

    public static boolean hasUpgrades(final ItemStack wirelessGrid) {
        if (!wirelessGrid.has(DataComponents.CUSTOM_DATA)) {
            return false;
        }
        final CompoundTag root = getRoot(wirelessGrid);
        if (!root.contains(UPGRADES_TAG)) {
            return false;
        }
        final CompoundTag upgrades = root.getCompound(UPGRADES_TAG);
        for (int slot = 0; slot < SLOT_COUNT; ++slot) {
            if (ContainerUtil.hasItemInSlot(upgrades, slot)) {
                return true;
            }
        }
        return false;
    }

    public static UpgradeContainer createContainer(final ItemStack wirelessGrid, final Player player) {
        final UpgradeContainer container = new UpgradeContainer(MoreUpgradeDestinations.WIRELESS_GRIDS, SLOT_COUNT);
        final CompoundTag root = getRoot(wirelessGrid);
        if (root.contains(UPGRADES_TAG)) {
            ContainerUtil.read(root.getCompound(UPGRADES_TAG), container, player.registryAccess());
        }
        return container;
    }

    public static ItemStack getUpgrade(final ItemStack wirelessGrid,
                                       final int slot,
                                       final Player player) {
        if (slot < 0 || slot >= SLOT_COUNT || wirelessGrid.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return createContainer(wirelessGrid, player).getItem(slot);
    }

    public static void setUpgrade(final ItemStack wirelessGrid,
                                  final int slot,
                                  final ItemStack upgrade,
                                  final Player player) {
        if (slot < 0 || slot >= SLOT_COUNT || wirelessGrid.isEmpty()) {
            return;
        }
        final UpgradeContainer container = createContainer(wirelessGrid, player);
        container.setItem(slot, upgrade.copy());
        save(container, wirelessGrid, player);
    }

    public static void save(final UpgradeContainer container,
                            final ItemStack wirelessGrid,
                            final Player player) {
        final CompoundTag customData = wirelessGrid.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        final CompoundTag root = customData.contains(MOD_ID) ? customData.getCompound(MOD_ID) : new CompoundTag();
        root.put(UPGRADES_TAG, ContainerUtil.write(container, player.registryAccess()));
        customData.put(MOD_ID, root);
        wirelessGrid.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
    }

    private static CompoundTag getRoot(final ItemStack wirelessGrid) {
        final CompoundTag customData = wirelessGrid.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return customData.contains(MOD_ID) ? customData.getCompound(MOD_ID) : new CompoundTag();
    }
}
