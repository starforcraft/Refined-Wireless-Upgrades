package com.ultramega.refinedwirelessupgrades.common.network;

import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeContainerMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public record CurioSlotUpdatePayload(int containerId, String key, int filter) implements CustomPacketPayload {
    public static final Type<CurioSlotUpdatePayload> TYPE = new Type<>(createInsertExportIdentifier("curio_slot_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CurioSlotUpdatePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, CurioSlotUpdatePayload::containerId,
        ByteBufCodecs.stringUtf8(256), CurioSlotUpdatePayload::key,
        ByteBufCodecs.VAR_INT, CurioSlotUpdatePayload::filter,
        CurioSlotUpdatePayload::new
    );

    public void handle(final Player player) {
        if (player.containerMenu instanceof UpgradeContainerMenu menu && menu.containerId == this.containerId) {
            menu.updateSelectedCurioSlot(this.key, this.filter);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
