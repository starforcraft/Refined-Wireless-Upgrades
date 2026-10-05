package com.ultramega.refinedwirelessupgrades.common.util;

import com.ultramega.refinedwirelessupgrades.common.ServerConfig;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.ItemContainerContents;

public record WirelessGridUpgradeState(ItemContainerContents items, int activeSlots) {
    public static final WirelessGridUpgradeState EMPTY = new WirelessGridUpgradeState(ItemContainerContents.EMPTY, ServerConfig.DEFAULT_WIRELESS_GRID_UPGRADE_SLOTS);

    public static final Codec<WirelessGridUpgradeState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemContainerContents.CODEC.fieldOf("items").forGetter(WirelessGridUpgradeState::items),
        Codec.intRange(1, ServerConfig.MAX_WIRELESS_GRID_UPGRADE_SLOTS).fieldOf("active_slots").forGetter(WirelessGridUpgradeState::activeSlots)
    ).apply(instance, WirelessGridUpgradeState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, WirelessGridUpgradeState> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
}
