package com.ultramega.rsinsertexportupgrade.common.menu;

import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.MOD_ID;

public final class MagnetConfiguration {
    public static final int FILTER_SIZE = 18;

    public static final String PICKUP_ALLOW_TAG = "PickupAllow";
    public static final String INSERT_ALLOW_TAG = "InsertAllow";
    public static final String TO_NETWORK_TAG = "ToNetwork";
    private static final String KEY = "MagnetConfiguration";

    private MagnetConfiguration() {
    }

    public static ResourceContainer createFilter() {
        return ResourceContainerImpl.createForFilter(RefinedStorageApi.INSTANCE.getItemResourceFactory(), FILTER_SIZE);
    }

    public static boolean getOption(final ItemStack stack, final String key) {
        return read(stack).getBoolean(key);
    }

    public static void setOption(final ItemStack stack, final String key, final boolean value) {
        final CompoundTag data = read(stack);
        data.putBoolean(key, value);
        write(stack, data);
    }

    public static void loadFilter(final ItemStack stack, final ResourceContainer filter, final boolean pickup, final HolderLookup.Provider registries) {
        final CompoundTag data = read(stack);
        final String key = pickup ? "PickupFilter" : "InsertFilter";
        if (data.contains(key)) {
            filter.fromTag(data.getCompound(key), registries);
        }
    }

    public static void saveFilter(final ItemStack stack, final ResourceContainer filter, final boolean pickup, final HolderLookup.Provider registries) {
        final CompoundTag data = read(stack);
        data.put(pickup ? "PickupFilter" : "InsertFilter", filter.toTag(registries));
        write(stack, data);
    }

    public static Filter createRuntimeFilter(final ItemStack stack, final boolean pickup, final HolderLookup.Provider registries) {
        final ResourceContainer container = createFilter();
        loadFilter(stack, container, pickup, registries);
        final Filter filter = new Filter();
        filter.setFilters(container.getUniqueResources());
        filter.setMode(getOption(stack, pickup ? PICKUP_ALLOW_TAG : INSERT_ALLOW_TAG) ? FilterMode.ALLOW : FilterMode.BLOCK);
        return filter;
    }

    private static CompoundTag read(final ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
            .getCompound(MOD_ID).getCompound(KEY);
    }

    private static void write(final ItemStack stack, final CompoundTag data) {
        final CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        final CompoundTag root = custom.getCompound(MOD_ID);
        root.put(KEY, data);
        custom.put(MOD_ID, root);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
    }
}
