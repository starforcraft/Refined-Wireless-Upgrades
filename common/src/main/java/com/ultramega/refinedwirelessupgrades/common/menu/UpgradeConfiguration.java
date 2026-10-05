package com.ultramega.refinedwirelessupgrades.common.menu;

import com.ultramega.refinedwirelessupgrades.common.item.BlockPickerUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.item.ExportUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.item.InsertUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.item.UpgradeFilterState;
import com.ultramega.refinedwirelessupgrades.common.registry.ModDataComponents;
import com.ultramega.refinedwirelessupgrades.common.util.ContainerSerialization;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public final class UpgradeConfiguration {
    public static final int DEFAULT_BLOCK_PICKER_AMOUNT = 1;
    public static final int MAX_BLOCK_PICKER_AMOUNT = 64;
    public static final int MAX_CURIO_SELECTIONS = 1024;

    private UpgradeConfiguration() {
    }

    public static int[] getSelectedInventorySlots(final ItemStack stack) {
        final int[] result = new int[UpgradeContainerMenu.INVENTORY_SLOT_COUNT];
        final List<Integer> stored = getSettings(stack).selectedInventorySlots();
        for (int slot = 0; slot < Math.min(stored.size(), result.length); ++slot) {
            result[slot] = stored.get(slot);
        }
        return result;
    }

    public static void setSelectedInventorySlots(final ItemStack stack, final int[] selectedInventorySlots) {
        final List<Integer> slots = Arrays.stream(selectedInventorySlots).limit(UpgradeContainerMenu.INVENTORY_SLOT_COUNT)
            .map(value -> Math.clamp(value, 0, UpgradeContainerMenu.FILTER_SLOT_COUNT)).boxed().toList();
        updateSettings(stack, settings -> settings.withSelectedInventorySlots(slots));
    }

    public static Map<String, Integer> getSelectedCurioSlots(final ItemStack stack) {
        return getSettings(stack).selectedCurioSlots();
    }

    public static void setSelectedCurioSlots(final ItemStack stack, final Map<String, Integer> selected) {
        final Map<String, Integer> sanitized = new HashMap<>();
        for (final var entry : selected.entrySet()) {
            if (entry.getKey().length() <= 256 && entry.getValue() > 0
                && entry.getValue() <= UpgradeContainerMenu.FILTER_SLOT_COUNT) {
                sanitized.put(entry.getKey(), entry.getValue());
                if (sanitized.size() >= MAX_CURIO_SELECTIONS) {
                    break;
                }
            }
        }
        updateSettings(stack, settings -> settings.withSelectedCurioSlots(sanitized));
    }

    public static FilterMode getFilterMode(final ItemStack stack) {
        return getSettings(stack).filterMode();
    }

    public static void setFilterMode(final ItemStack stack, final FilterMode mode) {
        updateSettings(stack, settings -> settings.withFilterMode(mode));
    }

    public static boolean isFuzzyMode(final ItemStack stack) {
        return getSettings(stack).fuzzyMode();
    }

    public static void setFuzzyMode(final ItemStack stack, final boolean fuzzyMode) {
        updateSettings(stack, settings -> settings.withFuzzyMode(fuzzyMode));
    }

    public static int getBlockPickerAmount(final ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.INSTANCE.getBlockPickerUpgradeState(), BlockPickerUpgradeState.EMPTY).amount();
    }

    public static void setBlockPickerAmount(final ItemStack stack, final int amount) {
        final var component = ModDataComponents.INSTANCE.getBlockPickerUpgradeState();
        stack.set(component, stack.getOrDefault(component, BlockPickerUpgradeState.EMPTY).withAmount(amount));
    }

    public static void loadFilter(final ItemStack stack, final ResourceContainer filter) {
        ContainerSerialization.restoreResources(getSettings(stack).filter(), filter);
    }

    public static void saveFilter(final ItemStack stack, final ResourceContainer filter) {
        updateSettings(stack, settings -> settings.withFilter(ContainerSerialization.captureResources(filter)));
    }

    public static void loadUpgrades(final ItemStack stack, final Container upgrades) {
        ContainerSerialization.restore(stack.getOrDefault(ModDataComponents.INSTANCE.getExportUpgradeState(), ExportUpgradeState.EMPTY).upgrades(), upgrades);
    }

    public static void saveUpgrades(final ItemStack stack, final Container upgrades) {
        final var component = ModDataComponents.INSTANCE.getExportUpgradeState();
        stack.set(component, stack.getOrDefault(component, ExportUpgradeState.EMPTY).withUpgrades(ContainerSerialization.capture(upgrades)));
    }

    public static RuntimeConfiguration getRuntimeConfiguration(final ItemStack stack) {
        final int[] selectedInventorySlots = getSelectedInventorySlots(stack);
        final Map<String, Integer> selectedCurioSlots = getSelectedCurioSlots(stack);
        final boolean hasSelectedSlots = Arrays.stream(selectedInventorySlots).anyMatch(value -> value > 0) || !selectedCurioSlots.isEmpty();
        final ResourceContainer filter = ResourceContainerImpl.createForFilter(UpgradeContainerMenu.FILTER_SLOT_COUNT);
        boolean hasStackUpgrade = false;
        boolean hasAutocraftingUpgrade = false;
        if (hasSelectedSlots) {
            loadFilter(stack, filter);
            final var upgrades = stack.getOrDefault(ModDataComponents.INSTANCE.getExportUpgradeState(), ExportUpgradeState.EMPTY).upgrades()
                .allItemsCopyStream().limit(UpgradeContainerMenu.UPGRADE_SLOT_COUNT).iterator();
            while (upgrades.hasNext()) {
                final ItemStack upgrade = upgrades.next();
                hasStackUpgrade |= upgrade.is(Items.INSTANCE.getStackUpgrade());
                hasAutocraftingUpgrade |= upgrade.is(Items.INSTANCE.getAutocraftingUpgrade());
            }
        }
        return new RuntimeConfiguration(selectedInventorySlots, selectedCurioSlots, hasSelectedSlots, getFilterMode(stack),
            isFuzzyMode(stack), filter, hasStackUpgrade, hasAutocraftingUpgrade);
    }

    private static boolean isExportUpgrade(final ItemStack stack) {
        return stack.is(com.ultramega.refinedwirelessupgrades.common.registry.Items.INSTANCE.getExportUpgrade());
    }

    private static UpgradeFilterState getSettings(final ItemStack stack) {
        return isExportUpgrade(stack)
            ? stack.getOrDefault(ModDataComponents.INSTANCE.getExportUpgradeState(), ExportUpgradeState.EMPTY).settings()
            : stack.getOrDefault(ModDataComponents.INSTANCE.getInsertUpgradeState(), InsertUpgradeState.EMPTY).settings();
    }

    private static void updateSettings(final ItemStack stack, final UnaryOperator<UpgradeFilterState> updater) {
        if (isExportUpgrade(stack)) {
            final var component = ModDataComponents.INSTANCE.getExportUpgradeState();
            final ExportUpgradeState state = stack.getOrDefault(component, ExportUpgradeState.EMPTY);
            stack.set(component, state.withSettings(updater.apply(state.settings())));
        } else {
            final var component = ModDataComponents.INSTANCE.getInsertUpgradeState();
            final InsertUpgradeState state = stack.getOrDefault(component, InsertUpgradeState.EMPTY);
            stack.set(component, state.withSettings(updater.apply(state.settings())));
        }
    }

    public record RuntimeConfiguration(int[] selectedInventorySlots,
                                       Map<String, Integer> selectedCurioSlots,
                                       boolean hasSelectedSlots,
                                       FilterMode filterMode,
                                       boolean fuzzyMode,
                                       ResourceContainer filter,
                                       boolean hasStackUpgrade,
                                       boolean hasAutocraftingUpgrade) {
    }
}
