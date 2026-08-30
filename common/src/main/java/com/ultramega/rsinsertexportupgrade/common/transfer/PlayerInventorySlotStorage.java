package com.ultramega.rsinsertexportupgrade.common.transfer;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class PlayerInventorySlotStorage implements Storage {
    private final ServerPlayer player;
    private final Inventory inventory;
    private final int slotIndex;

    public PlayerInventorySlotStorage(final ServerPlayer player, final int slotIndex) {
        this.player = player;
        this.inventory = player.getInventory();
        this.slotIndex = slotIndex;
    }

    @Override
    public Collection<ResourceAmount> getAll() {
        final ItemStack stack = this.getStack();
        if (stack.isEmpty()) {
            return List.of();
        }
        return List.of(new ResourceAmount(ItemResource.ofItemStack(stack), stack.getCount()));
    }

    @Override
    public long getStored() {
        return this.getStack().getCount();
    }

    @Override
    public long insert(final ResourceKey resource,
                       final long amount,
                       final Action action,
                       final Actor actor) {
        if (amount <= 0 || !(resource instanceof ItemResource itemResource) || !this.isValidSlot()) {
            return 0;
        }

        final ItemStack prototype = itemResource.toItemStack();
        final EquipmentSlot equipmentSlot = this.getEquipmentSlot();
        if (equipmentSlot != null && this.player.getEquipmentSlotForItem(prototype) != equipmentSlot) {
            return 0;
        }

        final ItemStack current = this.getStack();
        if (!current.isEmpty() && !ItemStack.isSameItemSameComponents(current, prototype)) {
            return 0;
        }

        final ItemStack limitStack = current.isEmpty() ? prototype : current;
        final int slotLimit = equipmentSlot == null ? this.inventory.getMaxStackSize(limitStack) : 1;
        final int maxStackSize = Math.min(slotLimit, limitStack.getMaxStackSize());
        final int space = maxStackSize - current.getCount();
        final int inserted = (int) Math.clamp(space, 0, amount);
        if (inserted == 0) {
            return 0;
        }

        if (action == Action.EXECUTE) {
            final ItemStack updated = current.isEmpty() ? itemResource.toItemStack(inserted) : current.copyWithCount(current.getCount() + inserted);
            this.setStack(updated);
        }
        return inserted;
    }

    @Override
    public long extract(final ResourceKey resource,
                        final long amount,
                        final Action action,
                        final Actor actor) {
        if (amount <= 0 || !(resource instanceof ItemResource itemResource) || !this.isValidSlot()) {
            return 0;
        }

        final ItemStack current = this.getStack();
        if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, itemResource.toItemStack()) || !this.mayRemoveArmor(current)) {
            return 0;
        }

        final int extracted = (int) Math.min(amount, current.getCount());
        if (action == Action.EXECUTE) {
            final ItemStack updated = current.copyWithCount(current.getCount() - extracted);
            this.setStack(updated.isEmpty() ? ItemStack.EMPTY : updated);
        }
        return extracted;
    }

    private boolean mayRemoveArmor(final ItemStack stack) {
        return this.getEquipmentSlot() == null
            || stack.isEmpty()
            || this.player.isCreative()
            || !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
    }

    private ItemStack getStack() {
        return this.isValidSlot() ? this.inventory.getItem(this.slotIndex) : ItemStack.EMPTY;
    }

    private void setStack(final ItemStack stack) {
        final EquipmentSlot equipmentSlot = this.getEquipmentSlot();
        if (equipmentSlot != null) {
            this.player.onEquipItem(equipmentSlot, this.getStack(), stack);
        }
        this.inventory.setItem(this.slotIndex, stack);
        this.inventory.setChanged();
    }

    private boolean isValidSlot() {
        return this.slotIndex >= 0 && this.slotIndex < this.inventory.getContainerSize();
    }

    @Nullable
    private EquipmentSlot getEquipmentSlot() {
        return switch (this.slotIndex) {
            case 36 -> EquipmentSlot.FEET;
            case 37 -> EquipmentSlot.LEGS;
            case 38 -> EquipmentSlot.CHEST;
            case 39 -> EquipmentSlot.HEAD;
            default -> null;
        };
    }
}
