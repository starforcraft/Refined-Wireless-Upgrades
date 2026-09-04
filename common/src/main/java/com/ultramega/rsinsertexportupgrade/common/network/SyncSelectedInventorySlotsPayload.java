package com.ultramega.rsinsertexportupgrade.common.network;

import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeContainerMenu;

import java.util.Arrays;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public record SyncSelectedInventorySlotsPayload(int containerId, int[] selectedInventorySlots) implements CustomPacketPayload {
    public static final Type<SyncSelectedInventorySlotsPayload> TYPE = new Type<>(createInsertExportIdentifier("sync_selected_inventory_slots"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSelectedInventorySlotsPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SyncSelectedInventorySlotsPayload::containerId,
        PayloadStreamCodecs.SELECTED_INVENTORY_SLOTS, SyncSelectedInventorySlotsPayload::selectedInventorySlots,
        SyncSelectedInventorySlotsPayload::new
    );

    public SyncSelectedInventorySlotsPayload {
        selectedInventorySlots = Arrays.copyOf(selectedInventorySlots, UpgradeContainerMenu.INVENTORY_SLOT_COUNT);
    }

    public void handle(final Player player) {
        if (player.containerMenu instanceof UpgradeContainerMenu menu && menu.containerId == this.containerId) {
            menu.receiveSelectedInventorySlots(this.selectedInventorySlots);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
