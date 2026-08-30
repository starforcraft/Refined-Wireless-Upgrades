package com.ultramega.rsinsertexportupgrade.common.menu;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class UpgradeArmorSlot extends UpgradePlayerSlot {
    private final Player owner;
    private final EquipmentSlot equipmentSlot;
    private final ResourceLocation emptyIcon;

    public UpgradeArmorSlot(final Container container,
                            final Player owner,
                            final EquipmentSlot equipmentSlot,
                            final int slot,
                            final int x,
                            final int y,
                            final ResourceLocation emptyIcon) {
        super(container, slot, x, y);
        this.owner = owner;
        this.equipmentSlot = equipmentSlot;
        this.emptyIcon = emptyIcon;
    }

    @Override
    public void setByPlayer(final ItemStack newStack, final ItemStack oldStack) {
        this.owner.onEquipItem(this.equipmentSlot, oldStack, newStack);
        super.setByPlayer(newStack, oldStack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(final ItemStack stack) {
        return this.equipmentSlot == this.owner.getEquipmentSlotForItem(stack);
    }

    @Override
    public boolean mayPickup(final Player player) {
        final ItemStack stack = this.getItem();
        return (stack.isEmpty() || player.isCreative() || !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE))
            && super.mayPickup(player);
    }

    @Override
    @Nullable
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, this.emptyIcon);
    }
}
