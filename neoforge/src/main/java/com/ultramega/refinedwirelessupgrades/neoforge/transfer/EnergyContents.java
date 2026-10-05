package com.ultramega.refinedwirelessupgrades.neoforge.transfer;

import com.ultramega.refinedwirelessupgrades.common.transfer.ItemContentsStorage.Change;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.neoforge.support.resource.SimpleItemStackResourceHandler;

import java.util.List;
import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;

final class EnergyContents {
    private EnergyContents() {
    }

    static void addContents(final ItemStack stack, final List<ResourceAmount> contents) {
        final var handler = stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack));
        if (handler != null && handler.getAmountAsLong() > 0) {
            AddonResourceKeys.energy().ifPresent(key -> contents.add(new ResourceAmount(key, handler.getAmountAsLong())));
        }
    }

    static Optional<Change> transfer(final ItemStack copy, final ResourceKey resource, final long amount, final boolean insert) {
        if (AddonResourceKeys.energy().filter(resource::equals).isEmpty()) {
            return Optional.empty();
        }
        final var inventory = SimpleItemStackResourceHandler.forStack(copy);
        final var handler = copy.getCapability(Capabilities.Energy.ITEM, ItemAccess.forHandlerIndex(inventory, 0));
        if (handler == null) {
            return Optional.empty();
        }
        try (Transaction transaction = Transaction.openRoot()) {
            final int requested = (int) Math.min(amount, Integer.MAX_VALUE);
            final long moved = insert ? handler.insert(requested, transaction) : handler.extract(requested, transaction);
            transaction.commit();
            return Optional.of(new Change(inventory.getStack(), moved));
        }
    }
}
