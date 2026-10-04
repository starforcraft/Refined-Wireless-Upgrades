package com.ultramega.refinedwirelessupgrades.common.item;

import com.ultramega.refinedwirelessupgrades.common.Platform;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeRegistry;

import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;
import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportTranslationKey;

public class UpgradeItem extends AbstractUpgradeItem {
    private final LongSupplier energyUsage;
    private final Supplier<Component> helpText;

    public UpgradeItem(final UpgradeRegistry registry,
                       final LongSupplier energyUsage,
                       final Component helpText) {
        this(registry, energyUsage, () -> helpText);
    }

    public UpgradeItem(final UpgradeRegistry registry,
                       final LongSupplier energyUsage,
                       final Supplier<Component> helpText) {
        super(new Item.Properties(), registry, Component.empty());
        this.energyUsage = energyUsage;
        this.helpText = helpText;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        final var destinations = this.getDestinations();
        return destinations.isEmpty()
            ? Optional.empty()
            : Optional.of(new UpgradeDestinationTooltipComponent(destinations, this.helpText.get()));
    }

    @Override
    public long getEnergyUsage() {
        return this.energyUsage.getAsLong();
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return false;
    }

    public static UpgradeItem insertUpgrade() {
        return new UpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            () -> Platform.getConfig().getUpgrade().getInsertUpgradeEnergyUsage(),
            createInsertExportTranslation("item", "insert_upgrade.help")
        );
    }

    public static UpgradeItem exportUpgrade() {
        return new UpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            () -> Platform.getConfig().getUpgrade().getExportUpgradeEnergyUsage(),
            createInsertExportTranslation("item", "export_upgrade.help")
        );
    }

    public static UpgradeItem blockPickerUpgrade() {
        return new UpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            () -> Platform.getConfig().getUpgrade().getBlockPickerUpgradeEnergyUsage(),
            createInsertExportTranslation("item", "block_picker_upgrade.help")
        );
    }

    public static UpgradeItem magnetUpgrade() {
        return new UpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            () -> Platform.getConfig().getUpgrade().getMagnetUpgradeEnergyUsage(),
            createInsertExportTranslation("item", "magnet_upgrade.help")
        );
    }

    public static UpgradeItem energyCapacityUpgrade() {
        return new UpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            () -> 0,
            () -> Component.translatable(
                createInsertExportTranslationKey("item", "energy_capacity_upgrade.help"),
                Math.max(0, Platform.getConfig().getUpgrade().getEnergyCapacityUpgradeCapacity())
            )
        );
    }
}
