package com.ultramega.refinedwirelessupgrades.fabric.transfer;

import com.ultramega.refinedwirelessupgrades.common.transfer.ItemContentsStorage;
import com.ultramega.refinedwirelessupgrades.common.transfer.ItemContentsStorage.Change;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.fabric.util.SimpleSingleStackStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil.ofFluidVariant;
import static com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil.toFluidVariant;

public final class FabricContentsAdapter implements ItemContentsStorage.Adapter {
    @Override
    public List<ResourceAmount> getContents(final ItemStack stack) {
        final var item = new SimpleSingleStackStorage(stack.copy());
        final var storage = FluidStorage.ITEM.find(item.getStack(), ContainerItemContext.ofSingleSlot(item));
        final List<ResourceAmount> contents = new ArrayList<>();
        if (storage != null) {
            for (final var view : storage) {
                if (!view.isResourceBlank() && view.getAmount() > 0) {
                    contents.add(new ResourceAmount(ofFluidVariant(view.getResource()), view.getAmount()));
                }
            }
        }
        return contents;
    }

    @Override
    public Optional<Change> transfer(final ItemStack copy, final ResourceKey resource, final long amount, final boolean insert) {
        if (!(resource instanceof FluidResource fluid)) {
            return Optional.empty();
        }
        final var item = new SimpleSingleStackStorage(copy);
        final var storage = FluidStorage.ITEM.find(copy, ContainerItemContext.ofSingleSlot(item));
        if (storage == null) {
            return Optional.empty();
        }
        try (Transaction tx = Transaction.openOuter()) {
            final long moved = insert ? storage.insert(toFluidVariant(fluid), amount, tx) : storage.extract(toFluidVariant(fluid), amount, tx);
            tx.commit();
            return Optional.of(new Change(item.getStack(), moved));
        }
    }
}
