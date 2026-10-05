package com.ultramega.refinedwirelessupgrades.common.api.upgrade;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Context valid only during a server tick callback. Do not retain it.
 * {@code upgradeStack} is a snapshot for inspection, not a writable inventory slot.
 * {@code wirelessGrid} is the live grid; do not remove it or modify its installed upgrades here.
 * Energy changes should use {@code networkItemContext.drainEnergy(...)}.
 * The caller guarantees an active, connected grid, but addon-specific security checks are
 * the callback's responsibility (for example INSERT, EXTRACT or AUTOCRAFTING permission).
 */
@API(status = Status.STABLE)
public record WirelessGridUpgradeContext(ServerPlayer player,
                                         ItemStack wirelessGrid,
                                         PlayerSlotReference wirelessGridSlot,
                                         int upgradeSlot,
                                         ItemStack upgradeStack,
                                         Network network,
                                         NetworkItemContext networkItemContext) {
}
