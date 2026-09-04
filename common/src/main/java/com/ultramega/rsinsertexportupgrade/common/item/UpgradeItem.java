package com.ultramega.rsinsertexportupgrade.common.item;

import com.ultramega.rsinsertexportupgrade.common.Platform;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeRegistry;

import java.util.function.LongSupplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;

public class UpgradeItem extends AbstractUpgradeItem {
    private final LongSupplier energyUsage;

    public UpgradeItem(final UpgradeRegistry registry,
                       final LongSupplier energyUsage,
                       final Component helpText) {
        super(new Item.Properties(), registry, helpText);
        this.energyUsage = energyUsage;
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
            Platform.getConfig().getUpgrade()::getInsertUpgradeEnergyUsage,
            createInsertExportTranslation("item", "insert_upgrade.help")
        );
    }

    public static UpgradeItem exportUpgrade() {
        return new UpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            Platform.getConfig().getUpgrade()::getExportUpgradeEnergyUsage,
            createInsertExportTranslation("item", "export_upgrade.help")
        );
    }

    public static UpgradeItem blockPickerUpgrade() {
        return new UpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry(),
            Platform.getConfig().getUpgrade()::getBlockPickerUpgradeEnergyUsage,
            createInsertExportTranslation("item", "block_picker_upgrade.help")
        );
    }
}
