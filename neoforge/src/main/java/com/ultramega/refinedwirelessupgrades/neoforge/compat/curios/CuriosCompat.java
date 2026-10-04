package com.ultramega.refinedwirelessupgrades.neoforge.compat.curios;

import com.ultramega.refinedwirelessupgrades.common.compat.curios.CuriosBridge;
import com.ultramega.refinedwirelessupgrades.common.transfer.ItemStackAccess;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.ISlotType;

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
            final ResourceLocation icon = CuriosApi.getSlot(identifier, player.level())
                .map(ISlotType::getIcon).orElse(ResourceLocation.fromNamespaceAndPath("curios", "slot/empty_curio_slot"));
            final var active = stacks.getActiveStates();
            for (int index = 0; index < stacks.getSlots(); ++index) {
                if (index < active.size() && !active.get(index)) {
                    continue;
                }
                final int slot = index;
                final var handler = stacks.getStacks();
                slots.add(new CuriosBridge.Slot(identifier, index, stacks.getStacks().getStackInSlot(index), icon,
                    new CurioSlotStorage(handler, slot), new ItemStackAccess(
                        () -> slot < handler.getSlots() ? handler.getStackInSlot(slot) : ItemStack.EMPTY,
                        updated -> slot < handler.getSlots() && (ItemStack.isSameItem(handler.getStackInSlot(slot), updated) || handler.isItemValid(slot, updated)),
                        updated -> handler.setStackInSlot(slot, updated))));
            }
        }));
        return slots;
    }
}
