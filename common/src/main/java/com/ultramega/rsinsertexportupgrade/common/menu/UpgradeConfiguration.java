package com.ultramega.rsinsertexportupgrade.common.menu;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.MOD_ID;

public final class UpgradeConfiguration {
    public static final int DEFAULT_BLOCK_PICKER_AMOUNT = 1;
    public static final int MAX_BLOCK_PICKER_AMOUNT = 64;

    private static final String CONFIGURATION_TAG = "Configuration";
    private static final String FILTER_TAG = "Filter";
    private static final String UPGRADES_TAG = "Upgrades";
    private static final String SELECTED_INVENTORY_SLOTS_TAG = "SelectedInventorySlots";
    private static final String FILTER_MODE_TAG = "FilterMode";
    private static final String FUZZY_MODE_TAG = "FuzzyMode";
    private static final String BLOCK_PICKER_AMOUNT_TAG = "BlockPickerAmount";
    private static final int INVENTORY_SLOT_COUNT = UpgradeContainerMenu.INVENTORY_SLOT_COUNT;
    private static final int UPGRADE_SLOT_COUNT = 2;

    private UpgradeConfiguration() {
    }

    public static int[] getSelectedInventorySlots(final ItemStack stack) {
        return getSelectedInventorySlots(getConfiguration(stack));
    }

    private static int[] getSelectedInventorySlots(final CompoundTag configuration) {
        final int[] result = new int[INVENTORY_SLOT_COUNT];
        final int[] stored = configuration.getIntArray(SELECTED_INVENTORY_SLOTS_TAG);
        System.arraycopy(stored, 0, result, 0, Math.min(stored.length, result.length));
        return result;
    }

    public static void setSelectedInventorySlots(final ItemStack stack, final int[] selectedInventorySlots) {
        updateConfiguration(stack, configuration -> configuration.putIntArray(SELECTED_INVENTORY_SLOTS_TAG, selectedInventorySlots));
    }

    public static FilterMode getFilterMode(final ItemStack stack) {
        return getFilterMode(getConfiguration(stack));
    }

    private static FilterMode getFilterMode(final CompoundTag configuration) {
        final int ordinal = configuration.contains(FILTER_MODE_TAG) ? configuration.getInt(FILTER_MODE_TAG) : 1;
        return ordinal == FilterMode.ALLOW.ordinal() ? FilterMode.ALLOW : FilterMode.BLOCK;
    }

    public static void setFilterMode(final ItemStack stack, final FilterMode mode) {
        updateConfiguration(stack, configuration -> configuration.putInt(FILTER_MODE_TAG, mode.ordinal()));
    }

    public static boolean isFuzzyMode(final ItemStack stack) {
        return getConfiguration(stack).getBoolean(FUZZY_MODE_TAG);
    }

    public static void setFuzzyMode(final ItemStack stack, final boolean fuzzyMode) {
        updateConfiguration(stack, configuration -> configuration.putBoolean(FUZZY_MODE_TAG, fuzzyMode));
    }

    public static int getBlockPickerAmount(final ItemStack stack) {
        final CompoundTag configuration = getConfiguration(stack);
        if (!configuration.contains(BLOCK_PICKER_AMOUNT_TAG)) {
            return DEFAULT_BLOCK_PICKER_AMOUNT;
        }
        return Math.clamp(
            configuration.getInt(BLOCK_PICKER_AMOUNT_TAG),
            DEFAULT_BLOCK_PICKER_AMOUNT,
            MAX_BLOCK_PICKER_AMOUNT
        );
    }

    public static void setBlockPickerAmount(final ItemStack stack, final int amount) {
        updateConfiguration(stack, configuration -> configuration.putInt(
            BLOCK_PICKER_AMOUNT_TAG,
            Math.clamp(amount, DEFAULT_BLOCK_PICKER_AMOUNT, MAX_BLOCK_PICKER_AMOUNT)
        ));
    }

    public static void loadFilter(final ItemStack stack,
                                  final Container filter,
                                  final HolderLookup.Provider provider) {
        final CompoundTag configuration = getConfiguration(stack);
        if (configuration.contains(FILTER_TAG)) {
            ContainerUtil.read(configuration.getCompound(FILTER_TAG), filter, provider);
        }
    }

    public static void saveFilter(final ItemStack stack,
                                  final Container filter,
                                  final HolderLookup.Provider provider) {
        updateConfiguration(stack, configuration -> configuration.put(FILTER_TAG, ContainerUtil.write(filter, provider)));
    }

    public static void loadUpgrades(final ItemStack stack,
                                    final Container upgrades,
                                    final HolderLookup.Provider provider) {
        final CompoundTag configuration = getConfiguration(stack);
        if (configuration.contains(UPGRADES_TAG)) {
            ContainerUtil.read(configuration.getCompound(UPGRADES_TAG), upgrades, provider);
        }
    }

    public static void saveUpgrades(final ItemStack stack,
                                    final Container upgrades,
                                    final HolderLookup.Provider provider) {
        updateConfiguration(stack, configuration -> configuration.put(UPGRADES_TAG, ContainerUtil.write(upgrades, provider)));
    }

    public static RuntimeConfiguration getRuntimeConfiguration(final ItemStack stack,
                                                               final HolderLookup.Provider provider) {
        final CompoundTag configuration = getConfiguration(stack);
        final int[] selectedInventorySlots = getSelectedInventorySlots(configuration);
        final boolean hasSelectedSlots = hasSelectedSlots(selectedInventorySlots);
        final SimpleContainer filter = new SimpleContainer(UpgradeContainerMenu.FILTER_SLOT_COUNT);
        boolean hasStackUpgrade = false;
        boolean hasAutocraftingUpgrade = false;

        if (hasSelectedSlots && configuration.contains(FILTER_TAG)) {
            ContainerUtil.read(configuration.getCompound(FILTER_TAG), filter, provider);
        }
        if (hasSelectedSlots && configuration.contains(UPGRADES_TAG)) {
            final SimpleContainer upgrades = new SimpleContainer(UPGRADE_SLOT_COUNT);
            ContainerUtil.read(configuration.getCompound(UPGRADES_TAG), upgrades, provider);
            for (int slot = 0; slot < upgrades.getContainerSize(); ++slot) {
                final ItemStack upgrade = upgrades.getItem(slot);
                hasStackUpgrade |= upgrade.is(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getStackUpgrade());
                hasAutocraftingUpgrade |= upgrade.is(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getAutocraftingUpgrade());
            }
        }

        return new RuntimeConfiguration(
            selectedInventorySlots,
            hasSelectedSlots,
            getFilterMode(configuration),
            configuration.getBoolean(FUZZY_MODE_TAG),
            filter,
            hasStackUpgrade,
            hasAutocraftingUpgrade
        );
    }

    private static boolean hasSelectedSlots(final int[] selectedInventorySlots) {
        for (final int selectedInventorySlot : selectedInventorySlots) {
            if (selectedInventorySlot > 0) {
                return true;
            }
        }
        return false;
    }

    private static CompoundTag getConfiguration(final ItemStack stack) {
        final CompoundTag customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!customData.contains(MOD_ID)) {
            return new CompoundTag();
        }
        final CompoundTag root = customData.getCompound(MOD_ID);
        return root.contains(CONFIGURATION_TAG) ? root.getCompound(CONFIGURATION_TAG) : new CompoundTag();
    }

    private static void updateConfiguration(final ItemStack stack,
                                            final java.util.function.Consumer<CompoundTag> updater) {
        final CompoundTag customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        final CompoundTag root = customData.contains(MOD_ID) ? customData.getCompound(MOD_ID) : new CompoundTag();
        final CompoundTag configuration = root.contains(CONFIGURATION_TAG)
            ? root.getCompound(CONFIGURATION_TAG)
            : new CompoundTag();
        updater.accept(configuration);
        root.put(CONFIGURATION_TAG, configuration);
        customData.put(MOD_ID, root);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
    }

    public record RuntimeConfiguration(int[] selectedInventorySlots,
                                       boolean hasSelectedSlots,
                                       FilterMode filterMode,
                                       boolean fuzzyMode,
                                       SimpleContainer filter,
                                       boolean hasStackUpgrade,
                                       boolean hasAutocraftingUpgrade) {
    }
}
