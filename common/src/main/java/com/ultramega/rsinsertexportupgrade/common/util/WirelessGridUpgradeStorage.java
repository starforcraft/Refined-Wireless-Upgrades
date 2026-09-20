package com.ultramega.rsinsertexportupgrade.common.util;

import com.ultramega.rsinsertexportupgrade.common.Platform;
import com.ultramega.rsinsertexportupgrade.common.ServerConfig;
import com.ultramega.rsinsertexportupgrade.common.mixin.RefinedStorageApiAccessor;
import com.ultramega.rsinsertexportupgrade.common.mixin.RefinedStorageApiProxyInvoker;
import com.ultramega.rsinsertexportupgrade.common.registry.ContentIds;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.grid.WirelessGridItem;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.MOD_ID;

public final class WirelessGridUpgradeStorage {
    private static final String UPGRADES_TAG = "Upgrades";
    private static final String ACTIVE_SLOTS_TAG = "ActiveUpgradeSlots";
    private static final String WIRELESS_CRAFTING_GRID_CLASS = "com.refinedmods.refinedstorage.quartzarsenal.common.wirelesscraftinggrid.WirelessCraftingGridItem";
    private static final String WIRELESS_UNIVERSAL_GRID_CLASS = "com.ultramega.universalgrid.common.wirelessuniversalgrid.WirelessUniversalGridItem";

    private static int clientSlotCount = ServerConfig.DEFAULT_WIRELESS_GRID_UPGRADE_SLOTS;

    private WirelessGridUpgradeStorage() {
    }

    public static long getEnergyCapacityBonus(final ItemStack wirelessGrid) {
        final CompoundTag root = getRoot(wirelessGrid);
        final CompoundTag upgrades = root.getCompound(UPGRADES_TAG);
        final int slots = root.contains(ACTIVE_SLOTS_TAG)
            ? Math.clamp(root.getInt(ACTIVE_SLOTS_TAG), 1, ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS)
            : ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS;
        int cards = 0;
        for (int slot = 0; slot < slots && cards < WirelessGridEnergyStorage.MAX_CARDS; ++slot) {
            final CompoundTag item = upgrades.getCompound("i" + slot);
            final int count = item.contains("count") ? item.getInt("count") : 1;
            if (ContentIds.ENERGY_CAPACITY_UPGRADE.toString().equals(item.getString("id")) && count > 0) {
                cards += Math.min(count, WirelessGridEnergyStorage.MAX_CARDS - cards);
            }
        }
        if (cards == 0) {
            return 0;
        }
        final long perUpgrade = Platform.getConfig().getUpgrade().getEnergyCapacityUpgradeCapacity();
        return perUpgrade > Long.MAX_VALUE / cards ? Long.MAX_VALUE : cards * perUpgrade;
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
        for (int slot = 0; slot < ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS; ++slot) {
            if (ContainerUtil.hasItemInSlot(upgrades, slot)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupportedWirelessGrid(final ItemStack stack) {
        return !stack.isEmpty() && isSupportedWirelessGridItem(stack.getItem());
    }

    public static boolean isSupportedWirelessGridItem(final Item item) {
        if (item instanceof WirelessGridItem) {
            return true;
        }
        for (Class<?> type = item.getClass(); type != null; type = type.getSuperclass()) {
            if (WIRELESS_CRAFTING_GRID_CLASS.equals(type.getName()) || WIRELESS_UNIVERSAL_GRID_CLASS.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }

    public static UpgradeContainer createContainer(final ItemStack wirelessGrid, final Player player) {
        return createContainer(wirelessGrid, player, getSlotCount(player));
    }

    public static UpgradeContainer createContainer(final ItemStack wirelessGrid, final Player player, final int slotCount) {
        final UpgradeContainer container = new UpgradeContainer(MoreUpgradeDestinations.WIRELESS_GRIDS,
            Math.clamp(slotCount, 1, ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS));
        final CompoundTag root = getRoot(wirelessGrid);
        if (root.contains(UPGRADES_TAG)) {
            ContainerUtil.read(root.getCompound(UPGRADES_TAG), container, player.registryAccess());
        }
        return container;
    }

    public static int getSlotCount(final Player player) {
        return player.level().isClientSide
            ? clientSlotCount
            : Math.clamp(Platform.getServerConfig().getWirelessGridUpgradeSlots(), 1, ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS);
    }

    public static void setClientSlotCount(final int count) {
        clientSlotCount = Math.clamp(count, 1, ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS);
    }

    public static ItemStack getUpgrade(final ItemStack wirelessGrid,
                                       final int slot,
                                       final Player player) {
        if (slot < 0 || slot >= getSlotCount(player) || wirelessGrid.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return createContainer(wirelessGrid, player).getItem(slot);
    }

    public static ItemStack getUpgrade(final ItemStack wirelessGrid,
                                       final Item upgrade,
                                       final Player player) {
        if (!hasUpgrades(wirelessGrid)) {
            return ItemStack.EMPTY;
        }
        final UpgradeContainer container = createContainer(wirelessGrid, player);
        for (int slot = 0; slot < container.getContainerSize(); ++slot) {
            final ItemStack installedUpgrade = container.getItem(slot);
            if (installedUpgrade.is(upgrade)) {
                return installedUpgrade;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void setUpgrade(final ItemStack wirelessGrid,
                                  final int slot,
                                  final ItemStack upgrade,
                                  final Player player) {
        if (slot < 0 || slot >= getSlotCount(player) || wirelessGrid.isEmpty()) {
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
        // Preserve inactive slots even when the configured capacity was lowered
        final UpgradeContainer stored = new UpgradeContainer(MoreUpgradeDestinations.WIRELESS_GRIDS, ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS);
        ContainerUtil.read(root.getCompound(UPGRADES_TAG), stored, player.registryAccess());
        for (int slot = 0; slot < Math.min(container.getContainerSize(), stored.getContainerSize()); ++slot) {
            stored.setItem(slot, container.getItem(slot).copy());
        }
        root.put(UPGRADES_TAG, ContainerUtil.write(stored, player.registryAccess()));
        root.putInt(ACTIVE_SLOTS_TAG, Math.min(container.getContainerSize(), getSlotCount(player)));
        customData.put(MOD_ID, root);
        wirelessGrid.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
        if (!player.level().isClientSide) {
            RefinedStorageApi.INSTANCE.getEnergyStorage(wirelessGrid).ifPresent(storage -> {
                final var energyComponent = com.refinedmods.refinedstorage.common.content.DataComponents.INSTANCE.getEnergy();
                final long energy = wirelessGrid.getOrDefault(energyComponent, 0L);
                if (energy > storage.getCapacity()) {
                    wirelessGrid.set(energyComponent, storage.getCapacity());
                }
            });
        }
    }

    private static CompoundTag getRoot(final ItemStack wirelessGrid) {
        final CompoundTag customData = wirelessGrid.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return customData.contains(MOD_ID) ? customData.getCompound(MOD_ID) : new CompoundTag();
    }

    public static List<SlotReference> find(final Player player) {
        return ((RefinedStorageApiAccessor) ((RefinedStorageApiProxyInvoker) RefinedStorageApi.INSTANCE).wirelessUpgrades$ensureLoaded())
            .wirelessUpgrades$getSlotReferenceProvider().find(player, SupportedItems.ITEMS);
    }

    private static final class SupportedItems {
        private static final Set<Item> ITEMS = BuiltInRegistries.ITEM.stream()
            .filter(WirelessGridUpgradeStorage::isSupportedWirelessGridItem)
            .collect(Collectors.toUnmodifiableSet());
    }
}
