package com.ultramega.refinedwirelessupgrades.common.compat.curios;

import com.ultramega.refinedwirelessupgrades.common.transfer.ItemStackAccess;

import com.refinedmods.refinedstorage.api.storage.Storage;

import java.util.List;
import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CuriosBridge {
    private static Function<Player, List<Slot>> provider = player -> List.of();
    private static boolean loaded;

    private CuriosBridge() {
    }

    public static void register(final Function<Player, List<Slot>> slotProvider) {
        provider = slotProvider;
        loaded = true;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static List<Slot> getSlots(final Player player) {
        return provider.apply(player);
    }

    public record Slot(String identifier, int index, ItemStack stack, ResourceLocation icon, Storage storage, ItemStackAccess contentsAccess) {
        public String key() {
            return this.identifier + "/" + this.index;
        }
    }
}
