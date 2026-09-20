package com.ultramega.rsinsertexportupgrade.common;

import com.ultramega.rsinsertexportupgrade.common.api.client.WirelessGridSideButtonRegistry;
import com.ultramega.rsinsertexportupgrade.common.menu.MagnetContainerMenu;
import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeConfiguration;
import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeContainerMenu;
import com.ultramega.rsinsertexportupgrade.common.network.OpenUpgradePayload;
import com.ultramega.rsinsertexportupgrade.common.network.UpdateBlockPickerAmountPayload;
import com.ultramega.rsinsertexportupgrade.common.registry.Items;
import com.ultramega.rsinsertexportupgrade.common.registry.MenuTypes;
import com.ultramega.rsinsertexportupgrade.common.screen.MagnetScreen;
import com.ultramega.rsinsertexportupgrade.common.screen.UpgradeScreen;
import com.ultramega.rsinsertexportupgrade.common.screen.UpgradeScreenNavigation;
import com.ultramega.rsinsertexportupgrade.common.screen.widget.BlockPickerAmountSideButtonWidget;
import com.ultramega.rsinsertexportupgrade.common.screen.widget.UpgradeSideButtonWidget;
import com.ultramega.rsinsertexportupgrade.common.util.UpgradeType;

import com.refinedmods.refinedstorage.common.Platform;

public abstract class AbstractClientModInitializer {
    private static void registerSideButtons() {
        WirelessGridSideButtonRegistry.register(Items.INSTANCE.getInsertUpgrade(), 0,
            context -> new UpgradeSideButtonWidget(UpgradeType.INSERT, button -> openUpgrade(UpgradeType.INSERT)));
        WirelessGridSideButtonRegistry.register(Items.INSTANCE.getExportUpgrade(), 10,
            context -> new UpgradeSideButtonWidget(UpgradeType.EXPORT, button -> openUpgrade(UpgradeType.EXPORT)));
        WirelessGridSideButtonRegistry.register(Items.INSTANCE.getBlockPickerUpgrade(), 20,
            context -> new BlockPickerAmountSideButtonWidget(
                () -> UpgradeConfiguration.getBlockPickerAmount(context.getUpgradeStack()),
                amount -> Platform.INSTANCE.sendPacketToServer(
                    new UpdateBlockPickerAmountPayload(context.screen().getMenu().containerId, amount)), context.screen(), context.inventory()));
        WirelessGridSideButtonRegistry.register(Items.INSTANCE.getMagnetUpgrade(), 30,
            context -> new UpgradeSideButtonWidget(UpgradeType.MAGNET, button -> openUpgrade(UpgradeType.MAGNET)));
    }

    private static void openUpgrade(final UpgradeType type) {
        UpgradeScreenNavigation.rememberMousePosition();
        Platform.INSTANCE.sendPacketToServer(new OpenUpgradePayload(type));
    }

    protected static void registerScreens(final com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenRegistration registration) {
        registerSideButtons();
        registration.register(
            MenuTypes.INSTANCE.getInsertUpgrade(),
            (com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenConstructor<UpgradeContainerMenu, UpgradeScreen>)
                (menu, inventory, title) -> new UpgradeScreen(UpgradeType.INSERT, menu, inventory, title)
        );
        registration.register(
            MenuTypes.INSTANCE.getExportUpgrade(),
            (com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenConstructor<UpgradeContainerMenu, UpgradeScreen>)
                (menu, inventory, title) -> new UpgradeScreen(UpgradeType.EXPORT, menu, inventory, title)
        );
        registration.register(
            MenuTypes.INSTANCE.getMagnetUpgrade(),
            (com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenConstructor<MagnetContainerMenu, MagnetScreen>)
                MagnetScreen::new
        );
    }
}
