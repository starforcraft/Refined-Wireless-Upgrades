package com.ultramega.refinedwirelessupgrades.common.menu;

import com.ultramega.refinedwirelessupgrades.common.item.MagnetUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.registry.ModDataComponents;
import com.ultramega.refinedwirelessupgrades.common.util.ContainerSerialization;

import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import net.minecraft.world.item.ItemStack;

public final class MagnetConfiguration {
    public static final int FILTER_SIZE = 18;

    public static final String PICKUP_ALLOW_TAG = "PickupAllow";
    public static final String INSERT_ALLOW_TAG = "InsertAllow";
    public static final String TO_NETWORK_TAG = "ToNetwork";

    private MagnetConfiguration() {
    }

    private static MagnetUpgradeState getState(final ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.INSTANCE.getMagnetUpgradeState(), MagnetUpgradeState.EMPTY);
    }

    public static ResourceContainer createFilter() {
        return ResourceContainerImpl.createForFilter(RefinedStorageApi.INSTANCE.getItemResourceFactory(), FILTER_SIZE);
    }

    public static boolean getOption(final ItemStack stack, final String key) {
        final MagnetUpgradeState state = getState(stack);
        return switch (key) {
            case PICKUP_ALLOW_TAG -> state.pickupAllow();
            case INSERT_ALLOW_TAG -> state.insertAllow();
            case TO_NETWORK_TAG -> state.toNetwork();
            default -> throw new IllegalArgumentException("Unknown magnet option: " + key);
        };
    }

    public static void setOption(final ItemStack stack, final String key, final boolean value) {
        final MagnetUpgradeState state = getState(stack);
        final MagnetUpgradeState updated = switch (key) {
            case PICKUP_ALLOW_TAG -> state.withPickupAllow(value);
            case INSERT_ALLOW_TAG -> state.withInsertAllow(value);
            case TO_NETWORK_TAG -> state.withToNetwork(value);
            default -> throw new IllegalArgumentException("Unknown magnet option: " + key);
        };
        stack.set(ModDataComponents.INSTANCE.getMagnetUpgradeState(), updated);
    }

    public static void loadFilter(final ItemStack stack, final ResourceContainer filter, final boolean pickup) {
        final MagnetUpgradeState state = getState(stack);
        ContainerSerialization.restoreResources(pickup ? state.pickupFilter() : state.insertFilter(), filter);
    }

    public static void saveFilter(final ItemStack stack, final ResourceContainer filter, final boolean pickup) {
        final MagnetUpgradeState state = getState(stack);
        final var contents = ContainerSerialization.captureResources(filter);
        stack.set(ModDataComponents.INSTANCE.getMagnetUpgradeState(), pickup ? state.withPickupFilter(contents) : state.withInsertFilter(contents));
    }

    public static Filter createRuntimeFilter(final ItemStack stack, final boolean pickup) {
        final ResourceContainer container = createFilter();
        loadFilter(stack, container, pickup);
        final Filter filter = new Filter();
        filter.setFilters(container.getUniqueResources());
        filter.setMode(getOption(stack, pickup ? PICKUP_ALLOW_TAG : INSERT_ALLOW_TAG) ? FilterMode.ALLOW : FilterMode.BLOCK);
        return filter;
    }
}
