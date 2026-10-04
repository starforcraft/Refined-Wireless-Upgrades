package com.ultramega.refinedwirelessupgrades.common.network;

import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeContainerMenu;

import java.util.Map;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public record SyncSelectedCurioSlotsPayload(int containerId, Map<String, Integer> selected) implements CustomPacketPayload {
    public static final Type<SyncSelectedCurioSlotsPayload> TYPE = new Type<>(createInsertExportIdentifier("sync_selected_curio_slots"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSelectedCurioSlotsPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SyncSelectedCurioSlotsPayload::containerId,
        PayloadStreamCodecs.SELECTED_CURIO_SLOTS, SyncSelectedCurioSlotsPayload::selected,
        SyncSelectedCurioSlotsPayload::new
    );

    public SyncSelectedCurioSlotsPayload {
        selected = Map.copyOf(selected);
    }

    public void handle(final Player player) {
        if (player.containerMenu instanceof UpgradeContainerMenu menu && menu.containerId == this.containerId) {
            menu.receiveSelectedCurioSlots(this.selected);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
