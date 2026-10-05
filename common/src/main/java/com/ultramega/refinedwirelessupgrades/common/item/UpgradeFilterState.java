package com.ultramega.refinedwirelessupgrades.common.item;

import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeConfiguration;
import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeContainerMenu;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainerContents;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record UpgradeFilterState(List<Integer> selectedInventorySlots,
                                 Map<String, Integer> selectedCurioSlots,
                                 ResourceContainerContents filter,
                                 FilterMode filterMode,
                                 boolean fuzzyMode) {
    public static final UpgradeFilterState EMPTY = new UpgradeFilterState(List.of(), Map.of(), new ResourceContainerContents(List.of()), FilterMode.BLOCK, false);

    private static final Codec<Map<String, Integer>> CURIO_SELECTIONS_CODEC = Codec
        .unboundedMap(Codec.string(0, 256), Codec.intRange(1, UpgradeContainerMenu.FILTER_SLOT_COUNT))
        .validate(value -> value.size() <= UpgradeConfiguration.MAX_CURIO_SELECTIONS
            ? DataResult.success(value) : DataResult.error(() -> "Too many selected Curios slots"));

    public static final MapCodec<UpgradeFilterState> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.intRange(0, UpgradeContainerMenu.FILTER_SLOT_COUNT).listOf(0, UpgradeContainerMenu.INVENTORY_SLOT_COUNT)
            .optionalFieldOf("selected_inventory_slots", List.of()).forGetter(UpgradeFilterState::selectedInventorySlots),
        CURIO_SELECTIONS_CODEC.optionalFieldOf("selected_curio_slots", Map.of()).forGetter(UpgradeFilterState::selectedCurioSlots),
        Codec.lazyInitialized(() -> ResourceCodecs.CONTAINER_CONTENTS_CODEC)
            .optionalFieldOf("filter", EMPTY.filter()).forGetter(UpgradeFilterState::filter),
        Codec.BOOL.xmap(allow -> allow ? FilterMode.ALLOW : FilterMode.BLOCK, mode -> mode == FilterMode.ALLOW)
            .optionalFieldOf("allow_filter", FilterMode.BLOCK).forGetter(UpgradeFilterState::filterMode),
        Codec.BOOL.optionalFieldOf("fuzzy_mode", false).forGetter(UpgradeFilterState::fuzzyMode)
    ).apply(instance, UpgradeFilterState::new));
    public static final Codec<UpgradeFilterState> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, UpgradeFilterState> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public UpgradeFilterState {
        selectedInventorySlots = List.copyOf(selectedInventorySlots);
        selectedCurioSlots = Map.copyOf(selectedCurioSlots);
    }

    public UpgradeFilterState withSelectedInventorySlots(final List<Integer> slots) {
        return new UpgradeFilterState(slots, this.selectedCurioSlots, this.filter, this.filterMode, this.fuzzyMode);
    }

    public UpgradeFilterState withSelectedCurioSlots(final Map<String, Integer> slots) {
        return new UpgradeFilterState(this.selectedInventorySlots, slots, this.filter, this.filterMode, this.fuzzyMode);
    }

    public UpgradeFilterState withFilter(final ResourceContainerContents contents) {
        return new UpgradeFilterState(this.selectedInventorySlots, this.selectedCurioSlots, contents, this.filterMode, this.fuzzyMode);
    }

    public UpgradeFilterState withFilterMode(final FilterMode mode) {
        return new UpgradeFilterState(this.selectedInventorySlots, this.selectedCurioSlots, this.filter, mode, this.fuzzyMode);
    }

    public UpgradeFilterState withFuzzyMode(final boolean fuzzy) {
        return new UpgradeFilterState(this.selectedInventorySlots, this.selectedCurioSlots, this.filter, this.filterMode, fuzzy);
    }
}
