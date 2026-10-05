package com.ultramega.refinedwirelessupgrades.common.api.upgrade;

import com.ultramega.refinedwirelessupgrades.common.ServerConfig;
import com.ultramega.refinedwirelessupgrades.common.util.MoreUpgradeDestinations;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import static java.util.Objects.requireNonNull;

@API(status = Status.STABLE)
public final class WirelessGridUpgradeRegistry {
    private static final Map<Item, WirelessGridUpgradeFactory> FACTORIES = new ConcurrentHashMap<>();
    private static volatile long revision;

    private WirelessGridUpgradeRegistry() {
    }

    /** Allows one copy of an event-driven or passive upgrade without a server ticker */
    public static void register(final Item item) {
        register(item, 1, WirelessGridUpgradeTicker.NONE);
    }

    /** Allows one copy of an upgrade with a stateless server callback */
    public static void register(final Item item, final WirelessGridUpgradeTicker ticker) {
        register(item, 1, ticker);
    }

    /**
     * Registers the installation limit and a callback shared by all copies of this item.
     * Use registerFactory for per-installation cached configuration or transient state.
     */
    public static void register(final Item item, final int maxAmount, final WirelessGridUpgradeTicker ticker) {
        requireNonNull(ticker, "ticker");
        registerFactory(item, maxAmount, (stack) -> ticker);
    }

    /** Registers a factory and the maximum number of this upgrade allowed in a grid. */
    public static synchronized void registerFactory(final Item item, final int maxAmount, final WirelessGridUpgradeFactory factory) {
        requireNonNull(item, "item");
        requireNonNull(factory, "factory");
        if (maxAmount < 1 || maxAmount > ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS) {
            throw new IllegalArgumentException("maxAmount must be between 1 and " + ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS);
        }
        if (FACTORIES.containsKey(item)) {
            throw new IllegalArgumentException("Wireless grid upgrade already registered: " + item);
        }
        RefinedStorageApi.INSTANCE.getUpgradeRegistry()
            .forDestination(MoreUpgradeDestinations.WIRELESS_GRIDS)
            .add(item, maxAmount);
        FACTORIES.put(item, factory);
        ++revision;
    }

    /** Used by the runtime to invalidate cached configurations after registration */
    public static long getRevision() {
        return revision;
    }

    /** Creates behavior from a defensive snapshot; unknown and empty stacks never tick */
    public static WirelessGridUpgradeTicker createTicker(final ItemStack stack) {
        if (stack.isEmpty()) {
            return WirelessGridUpgradeTicker.NONE;
        }
        final WirelessGridUpgradeFactory factory = FACTORIES.get(stack.getItem());
        return factory == null ? WirelessGridUpgradeTicker.NONE : requireNonNull(factory.create(stack.copy()), "Upgrade factory returned a null ticker");
    }
}
