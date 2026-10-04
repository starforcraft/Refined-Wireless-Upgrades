package com.ultramega.refinedwirelessupgrades.neoforge.transfer;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;

final class AddonResourceKeys {
    static final ResourceLocation ENERGY = ResourceLocation.fromNamespaceAndPath("refinedtypes", "energy");
    static final ResourceLocation CHEMICAL = ResourceLocation.fromNamespaceAndPath("refinedstorage_mekanism_integration", "chemical");

    private AddonResourceKeys() {
    }

    static Optional<PlatformResourceKey> decode(final ResourceLocation type, final CompoundTag tag) {
        return RefinedStorageApi.INSTANCE.getResourceTypeRegistry().get(type)
            .flatMap(resourceType -> resourceType.getMapCodec().codec().parse(NbtOps.INSTANCE, tag).result());
    }

    static Optional<CompoundTag> encode(final ResourceKey resource, final ResourceLocation type) {
        if (!(resource instanceof PlatformResourceKey platform)
            || RefinedStorageApi.INSTANCE.getResourceTypeRegistry().getId(platform.getResourceType()).filter(type::equals).isEmpty()) {
            return Optional.empty();
        }
        return platform.getResourceType().getMapCodec().codec().encodeStart(NbtOps.INSTANCE, platform).result()
            .filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast);
    }

    static Optional<PlatformResourceKey> energy() {
        final CompoundTag tag = new CompoundTag();
        tag.putString("energy", "refinedtypes:fe");
        return decode(ENERGY, tag);
    }
}
