package com.ultramega.refinedwirelessupgrades.common.item;

import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeConfiguration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BlockPickerUpgradeState(int amount) {
    public static final BlockPickerUpgradeState EMPTY = new BlockPickerUpgradeState(UpgradeConfiguration.DEFAULT_BLOCK_PICKER_AMOUNT);

    public static final Codec<BlockPickerUpgradeState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.intRange(UpgradeConfiguration.DEFAULT_BLOCK_PICKER_AMOUNT, UpgradeConfiguration.MAX_BLOCK_PICKER_AMOUNT)
            .optionalFieldOf("amount", UpgradeConfiguration.DEFAULT_BLOCK_PICKER_AMOUNT).forGetter(BlockPickerUpgradeState::amount)
    ).apply(instance, BlockPickerUpgradeState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPickerUpgradeState> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public BlockPickerUpgradeState withAmount(final int newAmount) {
        return new BlockPickerUpgradeState(Math.clamp(newAmount, UpgradeConfiguration.DEFAULT_BLOCK_PICKER_AMOUNT, UpgradeConfiguration.MAX_BLOCK_PICKER_AMOUNT));
    }
}
