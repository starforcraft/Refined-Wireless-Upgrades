package com.ultramega.rsinsertexportupgrade.common.network;

import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeContainerMenu;

import java.util.Arrays;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public record SyncSelectedInventorySlotsPayload(int containerId, int[] selectedInventorySlots) implements CustomPacketPayload {
    public static final Type<SyncSelectedInventorySlotsPayload> TYPE = new Type<>(createInsertExportIdentifier("sync_selected_inventory_slots"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSelectedInventorySlotsPayload> STREAM_CODEC =
        StreamCodec.of(SyncSelectedInventorySlotsPayload::encode, SyncSelectedInventorySlotsPayload::decode);

    public SyncSelectedInventorySlotsPayload {
        selectedInventorySlots = Arrays.copyOf(selectedInventorySlots, UpgradeContainerMenu.INVENTORY_SLOT_COUNT);
    }

    public void handle(final Player player) {
        if (player.containerMenu instanceof UpgradeContainerMenu menu && menu.containerId == this.containerId) {
            menu.receiveSelectedInventorySlots(this.selectedInventorySlots);
        }
    }

    private static void encode(final RegistryFriendlyByteBuf buffer,
                               final SyncSelectedInventorySlotsPayload payload) {
        buffer.writeVarInt(payload.containerId);
        for (int i = 0; i < UpgradeContainerMenu.INVENTORY_SLOT_COUNT; ++i) {
            buffer.writeVarInt(payload.selectedInventorySlots[i]);
        }
    }

    private static SyncSelectedInventorySlotsPayload decode(final RegistryFriendlyByteBuf buffer) {
        final int containerId = buffer.readVarInt();
        final int[] selectedInventorySlots = new int[UpgradeContainerMenu.INVENTORY_SLOT_COUNT];
        for (int i = 0; i < selectedInventorySlots.length; ++i) {
            selectedInventorySlots[i] = buffer.readVarInt();
        }
        return new SyncSelectedInventorySlotsPayload(containerId, selectedInventorySlots);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
