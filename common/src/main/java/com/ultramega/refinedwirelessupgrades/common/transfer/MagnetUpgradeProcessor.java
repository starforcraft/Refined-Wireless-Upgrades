package com.ultramega.refinedwirelessupgrades.common.transfer;

import com.ultramega.refinedwirelessupgrades.common.Platform;
import com.ultramega.refinedwirelessupgrades.common.api.upgrade.WirelessGridUpgradeContext;
import com.ultramega.refinedwirelessupgrades.common.api.upgrade.WirelessGridUpgradeTicker;
import com.ultramega.refinedwirelessupgrades.common.menu.MagnetConfiguration;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.Comparator;
import java.util.List;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class MagnetUpgradeProcessor {
    private static final int INTERVAL = 5;
    private static final int MAX_ENTITIES = 32;
    private static final ThreadLocal<Pickup> CURRENT_PICKUP = new ThreadLocal<>();

    private MagnetUpgradeProcessor() {
    }

    public static WirelessGridUpgradeTicker createTicker(final ItemStack stack, final HolderLookup.Provider registries) {
        final Filter pickup = MagnetConfiguration.createRuntimeFilter(stack, true, registries);
        final Filter insert = MagnetConfiguration.createRuntimeFilter(stack, false, registries);
        final boolean toNetwork = MagnetConfiguration.getOption(stack, MagnetConfiguration.TO_NETWORK_TAG);
        return context -> tick(context, pickup, insert, toNetwork);
    }

    private static boolean tick(final WirelessGridUpgradeContext context, final Filter pickupFilter, final Filter insertFilter, final boolean toNetwork) {
        final var player = context.player();
        if (!player.isAlive() || player.isSpectator() || player.level().getGameTime() % INTERVAL != 0) {
            return false;
        }
        final double range = Math.clamp(Platform.getConfig().getUpgrade().getMagnetUpgradeRange(), 1, 32);
        final List<ItemEntity> candidates = player.level().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(range),
            entity -> entity.isAlive() && !entity.hasPickUpDelay()
                && !entity.getItem().isEmpty() && player.distanceToSqr(entity) <= range * range
                && pickupFilter.isAllowed(ItemResource.ofItemStack(entity.getItem())));
        candidates.sort(Comparator.comparingDouble(player::distanceToSqr));
        boolean changed = false;
        int attempts = 0;
        for (final ItemEntity entity : candidates) {
            if (!context.networkItemContext().isActive() || attempts >= MAX_ENTITIES) {
                break;
            }
            if (!entity.isAlive() || !player.hasLineOfSight(entity)) {
                continue;
            }
            ++attempts;
            final Pickup pickup = new Pickup(context, entity.getItem(), pickupFilter, insertFilter, toNetwork);
            final Pickup previous = CURRENT_PICKUP.get();
            CURRENT_PICKUP.set(pickup);
            try {
                entity.playerTouch(player);
                changed |= pickup.changed;
            } finally {
                if (previous == null) {
                    CURRENT_PICKUP.remove();
                } else {
                    CURRENT_PICKUP.set(previous);
                }
            }
        }
        return changed;
    }

    public static boolean addPickedUpStack(final Inventory inventory, final ItemStack stack) {
        final Pickup pickup = CURRENT_PICKUP.get();
        if (pickup == null || pickup.stack != stack || pickup.context.player().getInventory() != inventory) {
            return inventory.add(stack);
        }
        final WirelessGridUpgradeContext context = pickup.context;
        if (stack.isEmpty() || !context.networkItemContext().isActive() || !pickup.pickupFilter.isAllowed(ItemResource.ofItemStack(stack))) {
            return false;
        }
        final int before = stack.getCount();
        if (pickup.toNetwork && pickup.insertFilter.isAllowed(ItemResource.ofItemStack(stack))
            && SecurityHelper.isAllowed(context.player(), BuiltinPermission.INSERT, context.network())) {
            final StorageNetworkComponent storage = context.network().getComponent(StorageNetworkComponent.class);
            final long inserted = storage.insert(ItemResource.ofItemStack(stack), before, Action.EXECUTE, new PlayerActor(context.player()));
            stack.shrink(Math.clamp(inserted, 0, before));
        }
        // Rejected items and partial network insertions fall back to the inventory.
        // Inventory.add mutates the remainder therefore any unaccepted items stay in the original entity and don't require special handling
        if (!stack.isEmpty()) {
            inventory.add(stack);
        }
        final boolean changed = stack.getCount() < before;
        if (changed) {
            context.networkItemContext().drainEnergy(Math.max(0, Platform.getConfig().getUpgrade().getMagnetUpgradeEnergyUsage()));
            inventory.setChanged();
            pickup.changed = true;
        }
        return changed;
    }

    private static final class Pickup {
        private final WirelessGridUpgradeContext context;
        private final ItemStack stack;
        private final Filter pickupFilter;
        private final Filter insertFilter;
        private final boolean toNetwork;
        private boolean changed;

        private Pickup(final WirelessGridUpgradeContext context,
                       final ItemStack stack,
                       final Filter pickupFilter,
                       final Filter insertFilter,
                       final boolean toNetwork) {
            this.context = context;
            this.stack = stack;
            this.pickupFilter = pickupFilter;
            this.insertFilter = insertFilter;
            this.toNetwork = toNetwork;
        }
    }
}
