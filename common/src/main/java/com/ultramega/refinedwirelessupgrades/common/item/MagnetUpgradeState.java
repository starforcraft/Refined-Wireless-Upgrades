package com.ultramega.refinedwirelessupgrades.common.item;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainerContents;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MagnetUpgradeState(boolean pickupAllow,
                                 boolean insertAllow,
                                 boolean toNetwork,
                                 ResourceContainerContents pickupFilter,
                                 ResourceContainerContents insertFilter) {
    public static final MagnetUpgradeState EMPTY = new MagnetUpgradeState(false, false, false,
        new ResourceContainerContents(List.of()), new ResourceContainerContents(List.of()));

    private static final Codec<ResourceContainerContents> FILTER_CODEC = Codec.lazyInitialized(() -> ResourceCodecs.CONTAINER_CONTENTS_CODEC);
    public static final Codec<MagnetUpgradeState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("pickup_allow", false).forGetter(MagnetUpgradeState::pickupAllow),
        Codec.BOOL.optionalFieldOf("insert_allow", false).forGetter(MagnetUpgradeState::insertAllow),
        Codec.BOOL.optionalFieldOf("to_network", false).forGetter(MagnetUpgradeState::toNetwork),
        FILTER_CODEC.optionalFieldOf("pickup_filter", EMPTY.pickupFilter()).forGetter(MagnetUpgradeState::pickupFilter),
        FILTER_CODEC.optionalFieldOf("insert_filter", EMPTY.insertFilter()).forGetter(MagnetUpgradeState::insertFilter)
    ).apply(instance, MagnetUpgradeState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MagnetUpgradeState> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public MagnetUpgradeState withPickupAllow(final boolean allow) {
        return new MagnetUpgradeState(allow, this.insertAllow, this.toNetwork, this.pickupFilter, this.insertFilter);
    }

    public MagnetUpgradeState withInsertAllow(final boolean allow) {
        return new MagnetUpgradeState(this.pickupAllow, allow, this.toNetwork, this.pickupFilter, this.insertFilter);
    }

    public MagnetUpgradeState withToNetwork(final boolean enabled) {
        return new MagnetUpgradeState(this.pickupAllow, this.insertAllow, enabled, this.pickupFilter, this.insertFilter);
    }

    public MagnetUpgradeState withPickupFilter(final ResourceContainerContents contents) {
        return new MagnetUpgradeState(this.pickupAllow, this.insertAllow, this.toNetwork, contents, this.insertFilter);
    }

    public MagnetUpgradeState withInsertFilter(final ResourceContainerContents contents) {
        return new MagnetUpgradeState(this.pickupAllow, this.insertAllow, this.toNetwork, this.pickupFilter, contents);
    }
}
