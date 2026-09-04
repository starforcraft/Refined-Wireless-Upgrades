package com.ultramega.rsinsertexportupgrade.common;

import com.ultramega.rsinsertexportupgrade.common.item.UpgradeItem;
import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeContainerMenu;
import com.ultramega.rsinsertexportupgrade.common.registry.ContentIds;
import com.ultramega.rsinsertexportupgrade.common.registry.Items;
import com.ultramega.rsinsertexportupgrade.common.registry.MenuTypes;
import com.ultramega.rsinsertexportupgrade.common.util.MoreUpgradeDestinations;
import com.ultramega.rsinsertexportupgrade.common.util.UpgradeType;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;

import java.util.function.Supplier;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

public class AbstractModInitializer {
    protected void registerItems(final RegistryCallback<Item> callback) {
        final Supplier<AbstractUpgradeItem> insertUpgrade = callback.register(
            ContentIds.INSERT_UPGRADE,
            UpgradeItem::insertUpgrade
        );
        Items.INSTANCE.setInsertUpgrade(insertUpgrade);

        final Supplier<AbstractUpgradeItem> exportUpgrade = callback.register(
            ContentIds.EXPORT_UPGRADE,
            UpgradeItem::exportUpgrade
        );
        Items.INSTANCE.setExportUpgrade(exportUpgrade);

        final Supplier<AbstractUpgradeItem> blockPickerUpgrade = callback.register(
            ContentIds.BLOCK_PICKER_UPGRADE,
            UpgradeItem::blockPickerUpgrade
        );
        Items.INSTANCE.setBlockPickerUpgrade(blockPickerUpgrade);
    }

    protected void registerMenus(final RegistryCallback<MenuType<?>> callback, final MenuTypeFactory factory) {
        MenuTypes.INSTANCE.setInsertUpgrade(callback.register(
            ContentIds.INSERT_UPGRADE,
            () -> factory.create((syncId, inventory) -> UpgradeContainerMenu.client(UpgradeType.INSERT, syncId, inventory))
        ));
        MenuTypes.INSTANCE.setExportUpgrade(callback.register(
            ContentIds.EXPORT_UPGRADE,
            () -> factory.create((syncId, inventory) -> UpgradeContainerMenu.client(UpgradeType.EXPORT, syncId, inventory))
        ));
    }

    protected final void registerUpgradeMappings() {
        RefinedStorageApi.INSTANCE.getUpgradeRegistry().forDestination(MoreUpgradeDestinations.WIRELESS_GRIDS)
            .add(Items.INSTANCE.getInsertUpgrade(), 1)
            .add(Items.INSTANCE.getExportUpgrade(), 1)
            .add(Items.INSTANCE.getBlockPickerUpgrade(), 1);

        RefinedStorageApi.INSTANCE.getUpgradeRegistry().forDestination(MoreUpgradeDestinations.EXPORT_UPGRADE)
            .add(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getStackUpgrade())
            .add(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getAutocraftingUpgrade());
    }
}
