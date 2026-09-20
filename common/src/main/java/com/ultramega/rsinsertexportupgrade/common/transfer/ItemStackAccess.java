package com.ultramega.rsinsertexportupgrade.common.transfer;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.ItemStack;

public record ItemStackAccess(Supplier<ItemStack> get, Predicate<ItemStack> canReplace, Consumer<ItemStack> set) {
    public void replace(final ItemStack updated) {
        final ItemStack current = this.get.get();
        if (ItemStack.isSameItem(current, updated)) {
            // Keep equipped network-item contexts attached to the same stack instance
            final var removed = new ArrayList<DataComponentType<?>>();
            for (final TypedDataComponent<?> component : current.getComponents()) {
                if (!updated.has(component.type())) {
                    removed.add(component.type());
                }
            }
            removed.forEach(current::remove);
            current.applyComponents(updated.getComponents());
            current.setCount(updated.getCount());
            this.set.accept(current);
        } else {
            this.set.accept(updated);
        }
    }
}
