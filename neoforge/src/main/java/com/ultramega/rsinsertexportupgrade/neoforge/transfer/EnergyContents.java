package com.ultramega.rsinsertexportupgrade.neoforge.transfer;

import com.ultramega.rsinsertexportupgrade.common.transfer.ItemContentsStorage.Change;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;
import java.util.Optional;

import dev.technici4n.grandpower.api.ILongEnergyStorage;
import net.minecraft.world.item.ItemStack;

final class EnergyContents {
    private EnergyContents() {
    }

    static void addContents(final ItemStack stack, final List<ResourceAmount> contents) {
        final var handler = stack.getCapability(ILongEnergyStorage.ITEM);
        if (handler != null && handler.canExtract() && handler.getAmount() > 0) {
            AddonResourceKeys.energy().ifPresent(key -> contents.add(new ResourceAmount(key, handler.getAmount())));
        }
    }

    static Optional<Change> transfer(final ItemStack copy, final ResourceKey resource, final long amount, final boolean insert) {
        if (AddonResourceKeys.energy().filter(resource::equals).isEmpty()) {
            return Optional.empty();
        }
        final var handler = copy.getCapability(ILongEnergyStorage.ITEM);
        if (handler == null || (insert ? !handler.canReceive() : !handler.canExtract())) {
            return Optional.empty();
        }
        final long moved = insert ? handler.receive(amount, false) : handler.extract(amount, false);
        return Optional.of(new Change(copy, moved));
    }
}
