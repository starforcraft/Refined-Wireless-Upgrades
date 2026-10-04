package com.ultramega.refinedwirelessupgrades.common.network;

import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeContainerMenu;

import java.util.Arrays;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public record UpdateSelectedInventorySlotsPayload(int containerId, int[] selectedInventorySlots) implements CustomPacketPayload {
    public static final Type<UpdateSelectedInventorySlotsPayload> TYPE = new Type<>(createInsertExportIdentifier("update_selected_inventory_slots"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSelectedInventorySlotsPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, UpdateSelectedInventorySlotsPayload::containerId,
        PayloadStreamCodecs.SELECTED_INVENTORY_SLOTS, UpdateSelectedInventorySlotsPayload::selectedInventorySlots,
        UpdateSelectedInventorySlotsPayload::new
    );

    public UpdateSelectedInventorySlotsPayload {
        selectedInventorySlots = Arrays.copyOf(selectedInventorySlots, UpgradeContainerMenu.INVENTORY_SLOT_COUNT);
    }

    public void handle(final Player player) {
        if (player.containerMenu instanceof UpgradeContainerMenu menu && menu.containerId == this.containerId) {
            menu.updateSelectedInventorySlots(this.selectedInventorySlots);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
