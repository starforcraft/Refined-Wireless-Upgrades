package com.ultramega.rsinsertexportupgrade.neoforge.transfer;

import com.ultramega.rsinsertexportupgrade.common.transfer.ItemContentsStorage.Change;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.List;
import java.util.Optional;

import mekanism.api.Action;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;

final class ChemicalContents {
    private static final ItemCapability<IChemicalHandler, Void> CAPABILITY = ItemCapability.createVoid(
        ResourceLocation.fromNamespaceAndPath("mekanism", "chemical_handler"), IChemicalHandler.class);

    private ChemicalContents() {
    }

    static void addContents(final ItemStack stack, final List<ResourceAmount> contents) {
        final var handler = stack.getCapability(CAPABILITY);
        if (handler == null) {
            return;
        }
        for (int tank = 0; tank < handler.getChemicalTanks(); ++tank) {
            final var stored = handler.getChemicalInTank(tank);
            if (!stored.isEmpty()) {
                final CompoundTag tag = new CompoundTag();
                Chemical.HOLDER_CODEC.encodeStart(NbtOps.INSTANCE, stored.getChemicalHolder()).result().ifPresent(value -> {
                    tag.put("chemical", value);
                    AddonResourceKeys.decode(AddonResourceKeys.CHEMICAL, tag)
                        .ifPresent(key -> contents.add(new ResourceAmount(key, stored.getAmount())));
                });
            }
        }
    }

    static Optional<Change> transfer(final ItemStack copy, final ResourceKey resource, final long amount, final boolean insert) {
        final var chemicalHolder = AddonResourceKeys.encode(resource, AddonResourceKeys.CHEMICAL)
            .flatMap(tag -> Chemical.HOLDER_CODEC.parse(NbtOps.INSTANCE, tag.get("chemical")).result());
        final var handler = copy.getCapability(CAPABILITY);
        if (chemicalHolder.isEmpty() || handler == null) {
            return Optional.empty();
        }
        final ChemicalStack request = new ChemicalStack(chemicalHolder.get(), amount);
        final long moved = insert ? amount - handler.insertChemical(request, Action.EXECUTE).getAmount()
            : handler.extractChemical(request, Action.EXECUTE).getAmount();
        return Optional.of(new Change(copy, moved));
    }
}
