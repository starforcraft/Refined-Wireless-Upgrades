package com.ultramega.refinedwirelessupgrades.common.network;

import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeConfiguration;
import com.ultramega.refinedwirelessupgrades.common.registry.Items;
import com.ultramega.refinedwirelessupgrades.common.util.IGridUpgrade;

import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public record UpdateBlockPickerAmountPayload(int containerId, int amount) implements CustomPacketPayload {
    public static final Type<UpdateBlockPickerAmountPayload> TYPE = new Type<>(createInsertExportIdentifier("update_block_picker_amount"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateBlockPickerAmountPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, UpdateBlockPickerAmountPayload::containerId,
        ByteBufCodecs.VAR_INT, UpdateBlockPickerAmountPayload::amount,
        UpdateBlockPickerAmountPayload::new
    );

    public void handle(final Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
            || player.containerMenu.containerId != this.containerId
            || !(player.containerMenu instanceof IGridUpgrade)) {
            return;
        }

        final Slot blockPickerSlot = player.containerMenu.slots.stream()
            .filter(UpgradeSlot.class::isInstance)
            .filter(slot -> slot.getItem().is(Items.INSTANCE.getBlockPickerUpgrade()))
            .findFirst()
            .orElse(null);
        if (blockPickerSlot == null) {
            return;
        }

        final ItemStack configuredUpgrade = blockPickerSlot.getItem().copy();
        UpgradeConfiguration.setBlockPickerAmount(configuredUpgrade, this.amount);
        blockPickerSlot.set(configuredUpgrade);
        blockPickerSlot.setChanged();
        serverPlayer.containerMenu.broadcastChanges();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
