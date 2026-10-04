package com.ultramega.refinedwirelessupgrades.neoforge.compat.curios;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.Collection;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public final class CurioSlotStorage implements Storage {
    private final IItemHandler handler;
    private final int index;

    public CurioSlotStorage(final IItemHandler handler, final int index) {
        this.handler = handler;
        this.index = index;
    }

    private ItemStack getStack() {
        return this.isValid() ? this.handler.getStackInSlot(this.index) : ItemStack.EMPTY;
    }

    private boolean isValid() {
        return this.index >= 0 && this.index < this.handler.getSlots();
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        final ItemStack stack = this.getStack();
        return stack.isEmpty() ? List.of() : List.of(new ResourceAmount(ItemResource.ofItemStack(stack), stack.getCount()));
    }

    @Override
    public long getStored() {
        return this.getStack().getCount();
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (amount <= 0 || !(resource instanceof ItemResource item) || !this.isValid()) {
            return 0;
        }
        final int requested = (int) Math.min(amount, Integer.MAX_VALUE);
        final ItemStack remainder = this.handler.insertItem(this.index, item.toItemStack(requested), action == Action.SIMULATE);
        return requested - remainder.getCount();
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (amount <= 0 || !(resource instanceof ItemResource item) || !this.isValid() || !ItemStack.isSameItemSameComponents(this.getStack(), item.toItemStack())) {
            return 0;
        }
        return this.handler.extractItem(this.index, (int) Math.min(amount, Integer.MAX_VALUE), action == Action.SIMULATE).getCount();
    }
}
