package com.ultramega.refinedwirelessupgrades.neoforge.transfer;

import com.ultramega.refinedwirelessupgrades.common.transfer.ItemContentsStorage;
import com.ultramega.refinedwirelessupgrades.common.transfer.ItemContentsStorage.Change;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.ofFluidStack;
import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.toFluidStack;

public final class NeoForgeContentsAdapter implements ItemContentsStorage.Adapter {
    private final boolean energy = ModList.get().isLoaded("refinedtypes");
    private final boolean chemicals = ModList.get().isLoaded("mekanism") && ModList.get().isLoaded("refinedstorage_mekanism_integration");

    @Override
    public List<ResourceAmount> getContents(final ItemStack stack) {
        final List<ResourceAmount> contents = new ArrayList<>();
        final var fluid = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluid != null) {
            for (int tank = 0; tank < fluid.getTanks(); ++tank) {
                final var stored = fluid.getFluidInTank(tank);
                if (!stored.isEmpty()) {
                    contents.add(new ResourceAmount(ofFluidStack(stored), stored.getAmount()));
                }
            }
        }
        if (this.energy) {
            EnergyContents.addContents(stack, contents);
        }
        if (this.chemicals) {
            ChemicalContents.addContents(stack, contents);
        }
        return contents;
    }

    @Override
    public Optional<Change> transfer(final ItemStack copy, final ResourceKey resource, final long amount, final boolean insert) {
        if (resource instanceof FluidResource fluidResource) {
            final var handler = copy.getCapability(Capabilities.FluidHandler.ITEM);
            if (handler == null) {
                return Optional.empty();
            }
            final var fluid = toFluidStack(fluidResource, Math.min(amount, Integer.MAX_VALUE));
            final long moved = insert ? handler.fill(fluid, IFluidHandler.FluidAction.EXECUTE) : handler.drain(fluid, IFluidHandler.FluidAction.EXECUTE).getAmount();
            return Optional.of(new Change(handler.getContainer(), moved));
        }
        if (this.energy) {
            final var result = EnergyContents.transfer(copy, resource, amount, insert);
            if (result.isPresent()) {
                return result;
            }
        }
        return this.chemicals ? ChemicalContents.transfer(copy, resource, amount, insert) : Optional.empty();
    }
}
