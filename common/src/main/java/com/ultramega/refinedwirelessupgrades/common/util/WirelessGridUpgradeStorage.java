package com.ultramega.refinedwirelessupgrades.common.util;

import com.ultramega.refinedwirelessupgrades.common.Platform;
import com.ultramega.refinedwirelessupgrades.common.ServerConfig;
import com.ultramega.refinedwirelessupgrades.common.mixin.RefinedStorageApiAccessor;
import com.ultramega.refinedwirelessupgrades.common.mixin.RefinedStorageApiProxyInvoker;
import com.ultramega.refinedwirelessupgrades.common.registry.Items;
import com.ultramega.refinedwirelessupgrades.common.registry.ModDataComponents;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.grid.WirelessGridItem;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


public final class WirelessGridUpgradeStorage {
    private static final String WIRELESS_CRAFTING_GRID_CLASS = "com.refinedmods.refinedstorage.quartzarsenal.common.wirelesscraftinggrid.WirelessCraftingGridItem";
    private static final String WIRELESS_UNIVERSAL_GRID_CLASS = "com.ultramega.universalgrid.common.wirelessuniversalgrid.WirelessUniversalGridItem";

    private static int clientSlotCount = ServerConfig.DEFAULT_WIRELESS_GRID_UPGRADE_SLOTS;

    private WirelessGridUpgradeStorage() {
    }

    public static long getEnergyCapacityBonus(final ItemStack wirelessGrid) {
        final WirelessGridUpgradeState data = wirelessGrid.getOrDefault(ModDataComponents.INSTANCE.getGridUpgrades(), WirelessGridUpgradeState.EMPTY);
        int cards = 0;
        final var upgrades = data.items().allItemsCopyStream().limit(data.activeSlots()).iterator();
        while (upgrades.hasNext() && cards < WirelessGridEnergyStorage.MAX_CARDS) {
            final ItemStack upgrade = upgrades.next();
            if (upgrade.is(Items.INSTANCE.getEnergyCapacityUpgrade())) {
                cards += Math.min(upgrade.getCount(), WirelessGridEnergyStorage.MAX_CARDS - cards);
            }
        }
        if (cards == 0) {
            return 0;
        }
        final long perUpgrade = Platform.getConfig().getUpgrade().getEnergyCapacityUpgradeCapacity();
        return perUpgrade > Long.MAX_VALUE / cards ? Long.MAX_VALUE : cards * perUpgrade;
    }

    public static boolean hasUpgrades(final ItemStack wirelessGrid) {
        return wirelessGrid.getOrDefault(ModDataComponents.INSTANCE.getGridUpgrades(), WirelessGridUpgradeState.EMPTY)
            .items().nonEmptyItemCopyStream().findAny().isPresent();
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

    public static ObservableUpgradeContainer createContainer(final ItemStack wirelessGrid, final Player player) {
        return createContainer(wirelessGrid, player, getSlotCount(player));
    }

    public static ObservableUpgradeContainer createContainer(final ItemStack wirelessGrid, final Player player, final int slotCount) {
        final ObservableUpgradeContainer container = new ObservableUpgradeContainer(MoreUpgradeDestinations.WIRELESS_GRIDS,
            Math.clamp(slotCount, 1, ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS));
        ContainerSerialization.restore(wirelessGrid.getOrDefault(ModDataComponents.INSTANCE.getGridUpgrades(), WirelessGridUpgradeState.EMPTY).items(), container);
        return container;
    }

    public static int getSlotCount(final Player player) {
        return player.level().isClientSide()
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
        // Preserve inactive slots even when the configured capacity was lowered
        final UpgradeContainer stored = new UpgradeContainer(MoreUpgradeDestinations.WIRELESS_GRIDS, ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS);
        ContainerSerialization.restore(wirelessGrid.getOrDefault(ModDataComponents.INSTANCE.getGridUpgrades(), WirelessGridUpgradeState.EMPTY).items(), stored);
        for (int slot = 0; slot < Math.min(container.getContainerSize(), stored.getContainerSize()); ++slot) {
            stored.setItem(slot, container.getItem(slot).copy());
        }
        wirelessGrid.set(ModDataComponents.INSTANCE.getGridUpgrades(), new WirelessGridUpgradeState(ContainerSerialization.capture(stored),
            Math.min(container.getContainerSize(), getSlotCount(player))));
        if (!player.level().isClientSide()) {
            RefinedStorageApi.INSTANCE.getEnergyStorage(wirelessGrid, EnergyItemContext.READONLY).ifPresent(storage -> {
                final var energyComponent = com.refinedmods.refinedstorage.common.content.DataComponents.INSTANCE.getEnergy();
                final long energy = wirelessGrid.getOrDefault(energyComponent, 0L);
                if (energy > storage.getCapacity()) {
                    wirelessGrid.set(energyComponent, storage.getCapacity());
                }
            });
        }
    }

    public static List<PlayerSlotReference> find(final Player player) {
        return ((RefinedStorageApiAccessor) ((RefinedStorageApiProxyInvoker) RefinedStorageApi.INSTANCE).wirelessUpgrades$ensureLoaded())
            .wirelessUpgrades$getSlotReferenceProvider().find(player, SupportedItems.ITEMS);
    }

    private static final class SupportedItems {
        private static final Set<Item> ITEMS = BuiltInRegistries.ITEM.stream()
            .filter(WirelessGridUpgradeStorage::isSupportedWirelessGridItem)
            .collect(Collectors.toUnmodifiableSet());
    }
}
