package com.ultramega.rsinsertexportupgrade.common.network;

import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeContainerMenu;

import java.util.Arrays;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public record UpdateSelectedInventorySlotsPayload(int containerId, int[] selectedInventorySlots) implements CustomPacketPayload {
    public static final Type<UpdateSelectedInventorySlotsPayload> TYPE = new Type<>(createInsertExportIdentifier("update_selected_inventory_slots"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSelectedInventorySlotsPayload> STREAM_CODEC =
        StreamCodec.of(UpdateSelectedInventorySlotsPayload::encode, UpdateSelectedInventorySlotsPayload::decode);

    public UpdateSelectedInventorySlotsPayload {
        selectedInventorySlots = Arrays.copyOf(selectedInventorySlots, UpgradeContainerMenu.INVENTORY_SLOT_COUNT);
    }

    public void handle(final Player player) {
        if (player.containerMenu instanceof UpgradeContainerMenu menu && menu.containerId == this.containerId) {
            menu.updateSelectedInventorySlots(this.selectedInventorySlots);
        }
    }

    private static void encode(final RegistryFriendlyByteBuf buffer,
                               final UpdateSelectedInventorySlotsPayload payload) {
        buffer.writeVarInt(payload.containerId);
        for (int i = 0; i < UpgradeContainerMenu.INVENTORY_SLOT_COUNT; ++i) {
            buffer.writeVarInt(payload.selectedInventorySlots[i]);
        }
    }

    private static UpdateSelectedInventorySlotsPayload decode(final RegistryFriendlyByteBuf buffer) {
        final int containerId = buffer.readVarInt();
        final int[] selectedInventorySlots = new int[UpgradeContainerMenu.INVENTORY_SLOT_COUNT];
        for (int i = 0; i < selectedInventorySlots.length; ++i) {
            selectedInventorySlots[i] = buffer.readVarInt();
        }
        return new UpdateSelectedInventorySlotsPayload(containerId, selectedInventorySlots);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
