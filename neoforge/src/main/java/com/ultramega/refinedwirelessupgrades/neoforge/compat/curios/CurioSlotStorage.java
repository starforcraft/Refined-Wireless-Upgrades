package com.ultramega.refinedwirelessupgrades.neoforge.compat.curios;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil;

import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public final class CurioSlotStorage implements Storage {
    private final Supplier<ResourceHandler<ItemResource>> handlerProvider;
    private final int index;
    private final BooleanSupplier canExtract;

    public CurioSlotStorage(final Supplier<ResourceHandler<ItemResource>> handlerProvider, final int index,
                            final BooleanSupplier canExtract) {
        this.handlerProvider = handlerProvider;
        this.index = index;
        this.canExtract = canExtract;
    }

    private @Nullable ResourceHandler<ItemResource> getHandler() {
        final ResourceHandler<ItemResource> handler = this.handlerProvider.get();
        return this.index >= 0 && this.index < handler.size() ? handler : null;
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        final ResourceHandler<ItemResource> handler = this.getHandler();
        if (handler == null) {
            return List.of();
        }
        final ItemResource resource = handler.getResource(this.index);
        final long amount = handler.getAmountAsLong(this.index);
        return resource.isEmpty() || amount == 0 ? List.of()
            : List.of(new ResourceAmount(VariantUtil.ofPlatform(resource), amount));
    }

    @Override
    public long getStored() {
        final ResourceHandler<ItemResource> handler = this.getHandler();
        return handler == null ? 0 : handler.getAmountAsLong(this.index);
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final ItemResource item = VariantUtil.optionalItemToPlatform(resource);
        if (amount <= 0 || item == null || item.isEmpty()) {
            return 0;
        }
        final ResourceHandler<ItemResource> handler = this.getHandler();
        if (handler == null) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            final int inserted = handler.insert(this.index, item, (int) Math.min(amount, Integer.MAX_VALUE), transaction);
            if (action == Action.EXECUTE) {
                transaction.commit();
            }
            return inserted;
        }
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        final ItemResource item = VariantUtil.optionalItemToPlatform(resource);
        if (amount <= 0 || item == null || item.isEmpty()) {
            return 0;
        }
        final ResourceHandler<ItemResource> handler = this.getHandler();
        if (handler == null || !handler.getResource(this.index).equals(item) || !this.canExtract.getAsBoolean()) {
            return 0;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            final int extracted = handler.extract(this.index, item, (int) Math.min(amount, Integer.MAX_VALUE), transaction);
            if (action == Action.EXECUTE) {
                transaction.commit();
            }
            return extracted;
        }
    }
}
