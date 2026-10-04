package com.ultramega.refinedwirelessupgrades.common.network;

import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeMenuProvider;
import com.ultramega.refinedwirelessupgrades.common.registry.Items;
import com.ultramega.refinedwirelessupgrades.common.util.GridSlotReferenceAccessor;
import com.ultramega.refinedwirelessupgrades.common.util.IGridUpgrade;
import com.ultramega.refinedwirelessupgrades.common.util.UpgradeType;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public record OpenUpgradePayload(UpgradeType upgradeType) implements CustomPacketPayload {
    public static final Type<OpenUpgradePayload> TYPE = new Type<>(createInsertExportIdentifier("open_upgrade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenUpgradePayload> STREAM_CODEC = StreamCodec.composite(
        UpgradeType.STREAM_CODEC, OpenUpgradePayload::upgradeType,
        OpenUpgradePayload::new
    );

    public void handle(final Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
            || !(player.containerMenu instanceof IGridUpgrade)
            || !(player.containerMenu instanceof GridSlotReferenceAccessor gridSlotReferenceAccessor)) {
            return;
        }

        final SlotReference gridSlotReference = gridSlotReferenceAccessor.wirelessUpgrades$getGridSlotReference();
        if (gridSlotReference == null) {
            return;
        }

        if (!player.containerMenu.stillValid(player)) {
            return;
        }
        final Item expectedUpgrade = switch (this.upgradeType) {
            case INSERT -> Items.INSTANCE.getInsertUpgrade();
            case EXPORT -> Items.INSTANCE.getExportUpgrade();
            case MAGNET -> Items.INSTANCE.getMagnetUpgrade();
        };
        final Slot sourceSlot = player.containerMenu.slots.stream()
            .filter(UpgradeSlot.class::isInstance)
            .filter(slot -> slot.getItem().is(expectedUpgrade))
            .findFirst()
            .orElse(null);
        if (sourceSlot == null) {
            return;
        }

        Platform.INSTANCE.getMenuOpener().openMenu(
            serverPlayer,
            new UpgradeMenuProvider(this.upgradeType, gridSlotReference, sourceSlot.getContainerSlot())
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
