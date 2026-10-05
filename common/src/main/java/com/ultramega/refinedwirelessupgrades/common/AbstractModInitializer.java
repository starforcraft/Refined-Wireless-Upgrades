package com.ultramega.refinedwirelessupgrades.common;

import com.ultramega.refinedwirelessupgrades.common.api.upgrade.WirelessGridUpgradeRegistry;
import com.ultramega.refinedwirelessupgrades.common.api.upgrade.WirelessGridUpgradeTicker;
import com.ultramega.refinedwirelessupgrades.common.item.BlockPickerUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.item.ExportUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.item.InsertUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.item.MagnetUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.item.UpgradeItem;
import com.ultramega.refinedwirelessupgrades.common.menu.MagnetContainerMenu;
import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeContainerMenu;
import com.ultramega.refinedwirelessupgrades.common.registry.ContentIds;
import com.ultramega.refinedwirelessupgrades.common.registry.Items;
import com.ultramega.refinedwirelessupgrades.common.registry.MenuTypes;
import com.ultramega.refinedwirelessupgrades.common.registry.ModDataComponents;
import com.ultramega.refinedwirelessupgrades.common.transfer.MagnetUpgradeProcessor;
import com.ultramega.refinedwirelessupgrades.common.transfer.UpgradeProcessor;
import com.ultramega.refinedwirelessupgrades.common.util.MoreUpgradeDestinations;
import com.ultramega.refinedwirelessupgrades.common.util.UpgradeType;
import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridEnergyStorage;
import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridUpgradeState;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public class AbstractModInitializer {
    protected final void registerDataComponents(final RegistryCallback<DataComponentType<?>> callback) {
        ModDataComponents.INSTANCE.setGridUpgrades(callback.register(createInsertExportIdentifier("grid_upgrades"),
            () -> DataComponentType.<WirelessGridUpgradeState>builder().persistent(WirelessGridUpgradeState.CODEC)
                .networkSynchronized(WirelessGridUpgradeState.STREAM_CODEC).build()));
        ModDataComponents.INSTANCE.setInsertUpgradeState(callback.register(createInsertExportIdentifier("insert_upgrade_state"),
            () -> DataComponentType.<InsertUpgradeState>builder().persistent(InsertUpgradeState.CODEC)
                .networkSynchronized(InsertUpgradeState.STREAM_CODEC).build()));
        ModDataComponents.INSTANCE.setExportUpgradeState(callback.register(createInsertExportIdentifier("export_upgrade_state"),
            () -> DataComponentType.<ExportUpgradeState>builder().persistent(ExportUpgradeState.CODEC)
                .networkSynchronized(ExportUpgradeState.STREAM_CODEC).build()));
        ModDataComponents.INSTANCE.setBlockPickerUpgradeState(callback.register(createInsertExportIdentifier("block_picker_upgrade_state"),
            () -> DataComponentType.<BlockPickerUpgradeState>builder().persistent(BlockPickerUpgradeState.CODEC)
                .networkSynchronized(BlockPickerUpgradeState.STREAM_CODEC).build()));
        ModDataComponents.INSTANCE.setMagnetUpgradeState(callback.register(createInsertExportIdentifier("magnet_upgrade_state"),
            () -> DataComponentType.<MagnetUpgradeState>builder().persistent(MagnetUpgradeState.CODEC)
                .networkSynchronized(MagnetUpgradeState.STREAM_CODEC).build()));
    }

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
