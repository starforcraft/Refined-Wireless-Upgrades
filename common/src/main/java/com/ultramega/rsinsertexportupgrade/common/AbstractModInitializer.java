package com.ultramega.rsinsertexportupgrade.common;

import com.ultramega.rsinsertexportupgrade.common.api.upgrade.WirelessGridUpgradeRegistry;
import com.ultramega.rsinsertexportupgrade.common.api.upgrade.WirelessGridUpgradeTicker;
import com.ultramega.rsinsertexportupgrade.common.item.UpgradeItem;
import com.ultramega.rsinsertexportupgrade.common.menu.MagnetContainerMenu;
import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeContainerMenu;
import com.ultramega.rsinsertexportupgrade.common.registry.ContentIds;
import com.ultramega.rsinsertexportupgrade.common.registry.Items;
import com.ultramega.rsinsertexportupgrade.common.registry.MenuTypes;
import com.ultramega.rsinsertexportupgrade.common.transfer.MagnetUpgradeProcessor;
import com.ultramega.rsinsertexportupgrade.common.transfer.UpgradeProcessor;
import com.ultramega.rsinsertexportupgrade.common.util.MoreUpgradeDestinations;
import com.ultramega.rsinsertexportupgrade.common.util.UpgradeType;
import com.ultramega.rsinsertexportupgrade.common.util.WirelessGridEnergyStorage;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

public class AbstractModInitializer {
    protected void registerItems(final RegistryCallback<Item> callback) {
        Items.INSTANCE.setInsertUpgrade(callback.register(ContentIds.INSERT_UPGRADE, UpgradeItem::insertUpgrade));
        Items.INSTANCE.setExportUpgrade(callback.register(ContentIds.EXPORT_UPGRADE, UpgradeItem::exportUpgrade));
        Items.INSTANCE.setBlockPickerUpgrade(callback.register(ContentIds.BLOCK_PICKER_UPGRADE, UpgradeItem::blockPickerUpgrade));
        Items.INSTANCE.setMagnetUpgrade(callback.register(ContentIds.MAGNET_UPGRADE, UpgradeItem::magnetUpgrade));
        Items.INSTANCE.setEnergyCapacityUpgrade(callback.register(ContentIds.ENERGY_CAPACITY_UPGRADE, UpgradeItem::energyCapacityUpgrade));
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
        MenuTypes.INSTANCE.setMagnetUpgrade(callback.register(
            ContentIds.MAGNET_UPGRADE,
            () -> factory.create(MagnetContainerMenu::client))
        );
    }

    protected final void registerUpgradeMappings() {
        WirelessGridUpgradeRegistry.registerFactory(Items.INSTANCE.getInsertUpgrade(), 1, UpgradeProcessor::createInsertTicker);
        WirelessGridUpgradeRegistry.registerFactory(Items.INSTANCE.getExportUpgrade(), 1, UpgradeProcessor::createExportTicker);
        WirelessGridUpgradeRegistry.register(Items.INSTANCE.getBlockPickerUpgrade());
        WirelessGridUpgradeRegistry.register(Items.INSTANCE.getEnergyCapacityUpgrade(), WirelessGridEnergyStorage.MAX_CARDS, WirelessGridUpgradeTicker.NONE);
        WirelessGridUpgradeRegistry.registerFactory(Items.INSTANCE.getMagnetUpgrade(), 1, MagnetUpgradeProcessor::createTicker);

        RefinedStorageApi.INSTANCE.getUpgradeRegistry().forDestination(MoreUpgradeDestinations.EXPORT_UPGRADE)
            .add(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getStackUpgrade())
            .add(com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getAutocraftingUpgrade());
    }
}
