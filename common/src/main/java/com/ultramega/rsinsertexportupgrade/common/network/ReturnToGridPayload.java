package com.ultramega.rsinsertexportupgrade.common.network;

import com.ultramega.rsinsertexportupgrade.common.menu.MagnetContainerMenu;
import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeContainerMenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public record ReturnToGridPayload(int containerId) implements CustomPacketPayload {
    public static final Type<ReturnToGridPayload> TYPE = new Type<>(createInsertExportIdentifier("return_to_grid"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReturnToGridPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ReturnToGridPayload::containerId,
        ReturnToGridPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (player.containerMenu instanceof UpgradeContainerMenu menu && menu.containerId == this.containerId) {
            menu.returnToGrid(serverPlayer);
        } else if (player.containerMenu instanceof MagnetContainerMenu menu && menu.containerId == this.containerId && menu.stillValid(player)) {
            menu.returnToGrid(serverPlayer);
        }
    }
}
