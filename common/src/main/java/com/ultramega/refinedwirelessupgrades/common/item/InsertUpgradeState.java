package com.ultramega.refinedwirelessupgrades.common.item;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record InsertUpgradeState(UpgradeFilterState settings) {
    public static final InsertUpgradeState EMPTY = new InsertUpgradeState(UpgradeFilterState.EMPTY);

    public static final Codec<InsertUpgradeState> CODEC = UpgradeFilterState.CODEC
        .xmap(InsertUpgradeState::new, InsertUpgradeState::settings);
    public static final StreamCodec<RegistryFriendlyByteBuf, InsertUpgradeState> STREAM_CODEC = UpgradeFilterState.STREAM_CODEC
        .map(InsertUpgradeState::new, InsertUpgradeState::settings);

    public InsertUpgradeState withSettings(final UpgradeFilterState newSettings) {
        return new InsertUpgradeState(newSettings);
    }
}
