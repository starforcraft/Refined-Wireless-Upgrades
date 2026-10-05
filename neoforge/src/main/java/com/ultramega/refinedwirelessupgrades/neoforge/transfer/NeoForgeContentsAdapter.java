package com.ultramega.refinedwirelessupgrades.neoforge.transfer;

import com.ultramega.refinedwirelessupgrades.common.transfer.ItemContentsStorage;
import com.ultramega.refinedwirelessupgrades.common.transfer.ItemContentsStorage.Change;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.neoforge.support.resource.SimpleItemStackResourceHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.ofPlatform;
import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.toPlatform;

public final class NeoForgeContentsAdapter implements ItemContentsStorage.Adapter {
    private final boolean energy = ModList.get().isLoaded("refinedtypes");
//    private final boolean chemicals = ModList.get().isLoaded("mekanism") && ModList.get().isLoaded("refinedstorage_mekanism_integration");

    @Override
    public List<ResourceAmount> getContents(final ItemStack stack) {
        final List<ResourceAmount> contents = new ArrayList<>();
        final var fluid = stack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(stack));
        if (fluid != null) {
            for (int tank = 0; tank < fluid.size(); ++tank) {
                final var stored = fluid.getResource(tank);
                if (!stored.isEmpty()) {
                    contents.add(new ResourceAmount(ofPlatform(stored), fluid.getAmountAsLong(tank)));
                }
            }
        }
        if (this.energy) {
            EnergyContents.addContents(stack, contents);
        }
//        if (this.chemicals) {
//            ChemicalContents.addContents(stack, contents);
//        }
        return contents;
    }

    @Override
    public Optional<Change> transfer(final ItemStack copy, final ResourceKey resource, final long amount, final boolean insert) {
        if (resource instanceof FluidResource fluidResource) {
            final var inventory = SimpleItemStackResourceHandler.forStack(copy);
            final var handler = copy.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forHandlerIndex(inventory, 0));
            if (handler == null) {
                return Optional.empty();
            }
            final var fluid = toPlatform(fluidResource);
            try (Transaction transaction = Transaction.openRoot()) {
                final int requested = (int) Math.min(amount, Integer.MAX_VALUE);
                final long moved = insert ? handler.insert(fluid, requested, transaction)
                    : handler.extract(fluid, requested, transaction);
                transaction.commit();
                return Optional.of(new Change(inventory.getStack(), moved));
            }
        }
        if (this.energy) {
            final var result = EnergyContents.transfer(copy, resource, amount, insert);
            if (result.isPresent()) {
                return result;
            }
        }
        return /*this.chemicals ? ChemicalContents.transfer(copy, resource, amount, insert) :*/ Optional.empty();
    }
}
