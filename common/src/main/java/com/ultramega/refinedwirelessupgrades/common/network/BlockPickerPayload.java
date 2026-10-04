package com.ultramega.refinedwirelessupgrades.common.network;

import com.ultramega.refinedwirelessupgrades.common.transfer.BlockPickerUpgradeHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public record BlockPickerPayload(BlockPos blockPos, Direction direction) implements CustomPacketPayload {
    public static final Type<BlockPickerPayload> TYPE = new Type<>(createInsertExportIdentifier("block_picker"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPickerPayload> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, BlockPickerPayload::blockPos,
        Direction.STREAM_CODEC, BlockPickerPayload::direction,
        BlockPickerPayload::new
    );

    public void handle(final Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            BlockPickerUpgradeHandler.handle(serverPlayer, this.blockPos, this.direction);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
