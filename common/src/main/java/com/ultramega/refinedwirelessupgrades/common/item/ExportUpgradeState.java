package com.ultramega.refinedwirelessupgrades.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.component.ItemContainerContents;

public record ExportUpgradeState(UpgradeFilterState settings, ItemContainerContents upgrades) {
    public static final ExportUpgradeState EMPTY = new ExportUpgradeState(UpgradeFilterState.EMPTY, ItemContainerContents.EMPTY);

    public static final Codec<ExportUpgradeState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UpgradeFilterState.MAP_CODEC.forGetter(ExportUpgradeState::settings),
        ItemContainerContents.CODEC.optionalFieldOf("upgrades", ItemContainerContents.EMPTY).forGetter(ExportUpgradeState::upgrades)
    ).apply(instance, ExportUpgradeState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ExportUpgradeState> STREAM_CODEC = StreamCodec.composite(
        UpgradeFilterState.STREAM_CODEC, ExportUpgradeState::settings,
        ItemContainerContents.STREAM_CODEC, ExportUpgradeState::upgrades,
        ExportUpgradeState::new
    );

    public ExportUpgradeState withSettings(final UpgradeFilterState newSettings) {
        return new ExportUpgradeState(newSettings, this.upgrades);
    }

    public ExportUpgradeState withUpgrades(final ItemContainerContents contents) {
        return new ExportUpgradeState(this.settings, contents);
    }
}
