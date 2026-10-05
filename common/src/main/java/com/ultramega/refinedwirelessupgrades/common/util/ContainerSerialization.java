package com.ultramega.refinedwirelessupgrades.common.util;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainerContents;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public final class ContainerSerialization {
    private ContainerSerialization() {
    }

    public static ItemContainerContents capture(final Container container) {
        final List<ItemStack> items = new ArrayList<>(container.getContainerSize());
        for (int slot = 0; slot < container.getContainerSize(); ++slot) {
            items.add(container.getItem(slot));
        }
        return ItemContainerContents.fromItems(items);
    }

    public static void restore(final ItemContainerContents contents, final Container container) {
        final var items = contents.allItemsCopyStream().iterator();
        for (int slot = 0; slot < container.getContainerSize(); ++slot) {
            container.setItem(slot, items.hasNext() ? items.next() : ItemStack.EMPTY);
        }
    }

    public static ResourceContainerContents captureResources(final ResourceContainer container) {
        return ResourceContainerContents.of(container);
    }

    public static void restoreResources(final ResourceContainerContents contents, final ResourceContainer container) {
        container.clear();
        container.load(contents);
    }
}
