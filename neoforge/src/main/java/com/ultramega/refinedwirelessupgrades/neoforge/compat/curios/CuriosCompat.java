package com.ultramega.refinedwirelessupgrades.neoforge.compat.curios;

import com.ultramega.refinedwirelessupgrades.common.compat.curios.CuriosBridge;
import com.ultramega.refinedwirelessupgrades.common.transfer.ItemStackAccess;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.item.ItemResource;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosSlotTypes;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.common.inventory.CuriosResourceHandler;
import top.theillusivec4.curios.api.event.CurioCanUnequipEvent;

public final class CuriosCompat {
    private CuriosCompat() {
    }

    public static void register() {
        CuriosBridge.register(CuriosCompat::getSlots);
    }

    private static List<CuriosBridge.Slot> getSlots(final Player player) {
        final List<CuriosBridge.Slot> slots = new ArrayList<>();
        CuriosApi.getCuriosInventory(player).ifPresent(inventory -> inventory.getCurios().forEach((identifier, stacks) -> {
            if (!stacks.isVisible()) {
                return;
            }
            final var slotType = CuriosSlotTypes.getSlotType(identifier, player.level().isClientSide());
            final Identifier icon = slotType == null ? Identifier.fromNamespaceAndPath("curios", "slot/empty_curio_slot") : slotType.getIcon();
            final var active = stacks.getActiveStates();
            for (int index = 0; index < stacks.getSlots(); ++index) {
                if (index < active.size() && !active.get(index)) {
                    continue;
                }
                final int slot = index;
                final var handler = stacks.getStacks();
                slots.add(new CuriosBridge.Slot(identifier, index, stacks.getStacks().getStackInSlot(index), icon,
                    new CurioSlotStorage(() -> new CuriosResourceHandler(handler), slot, () -> canUnequip(handler.getStackInSlot(slot),
                        new SlotContext(identifier, player, slot, false, stacks.getRenders().get(slot)))),
                    new ItemStackAccess(
                        () -> slot < handler.getSlots() ? handler.getStackInSlot(slot) : ItemStack.EMPTY,
                        updated -> slot < handler.getSlots() && (ItemStack.isSameItem(handler.getStackInSlot(slot), updated)
                            || new CuriosResourceHandler(handler).isValid(slot, ItemResource.of(updated))),
                        updated -> handler.setStackInSlot(slot, updated))));
            }
        }));
        return slots;
    }

    private static boolean canUnequip(final ItemStack stack, final SlotContext context) {
        final boolean creative = context.entity() instanceof Player player && player.isCreative();
        final boolean originalResult = (stack.isEmpty() || creative
            || !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE))
            && CuriosApi.getCurio(stack).map(curio -> curio.canUnequip(context)).orElse(true);
        final CurioCanUnequipEvent event = new CurioCanUnequipEvent(stack, context, originalResult);
        NeoForge.EVENT_BUS.post(event);
        final TriState result = event.getUnequipResult();
        return result == TriState.TRUE || (result == TriState.DEFAULT && originalResult);
    }
}
