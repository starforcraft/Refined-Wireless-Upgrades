package com.ultramega.refinedwirelessupgrades.common.network;

import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridUpgradeStorage;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

// This is required because stupid fabric's AutoConfig doesn't sync server configs themselves
public record SyncUpgradeSlotCountPayload(int count) implements CustomPacketPayload {
    public static final Type<SyncUpgradeSlotCountPayload> TYPE = new Type<>(createInsertExportIdentifier("sync_upgrade_slot_count"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncUpgradeSlotCountPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SyncUpgradeSlotCountPayload::count,
        SyncUpgradeSlotCountPayload::new
    );

    public void handle() {
        WirelessGridUpgradeStorage.setClientSlotCount(this.count);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
