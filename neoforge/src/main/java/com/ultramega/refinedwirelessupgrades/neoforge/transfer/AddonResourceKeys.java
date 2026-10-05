package com.ultramega.refinedwirelessupgrades.neoforge.transfer;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;

final class AddonResourceKeys {
    static final Identifier ENERGY = Identifier.fromNamespaceAndPath("refinedtypes", "energy");
    static final Identifier CHEMICAL = Identifier.fromNamespaceAndPath("refinedstorage_mekanism_integration", "chemical");

    private AddonResourceKeys() {
    }

    static <T> Optional<PlatformResourceKey> decode(final Identifier type, final Codec<T> codec, final T value) {
        return codec.encodeStart(JsonOps.INSTANCE, value).result().flatMap(encoded ->
            RefinedStorageApi.INSTANCE.getResourceTypeRegistry().get(type)
                .flatMap(resourceType -> resourceType.getMapCodec().codec().parse(JsonOps.INSTANCE, encoded).result()));
    }

    static <T> Optional<T> encode(final ResourceKey resource, final Identifier type, final Codec<T> codec) {
        if (!(resource instanceof PlatformResourceKey platform)
            || RefinedStorageApi.INSTANCE.getResourceTypeRegistry().getId(platform.getResourceType()).filter(type::equals).isEmpty()) {
            return Optional.empty();
        }
        return platform.getResourceType().getMapCodec().codec().encodeStart(JsonOps.INSTANCE, platform).result()
            .flatMap(encoded -> codec.parse(JsonOps.INSTANCE, encoded).result());
    }

    static Optional<PlatformResourceKey> energy() {
        return decode(ENERGY, Codec.STRING.fieldOf("energy").codec(), "refinedtypes:fe");
    }
}
