package com.ultramega.rsinsertexportupgrade.common;

import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeContainerMenu;
import com.ultramega.rsinsertexportupgrade.common.registry.MenuTypes;
import com.ultramega.rsinsertexportupgrade.common.screen.UpgradeScreen;
import com.ultramega.rsinsertexportupgrade.common.util.UpgradeType;

public abstract class AbstractClientModInitializer {
    protected static void registerScreens(final com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenRegistration registration) {
        registration.register(
            MenuTypes.INSTANCE.getInsertUpgrade(),
            (com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenConstructor<UpgradeContainerMenu, UpgradeScreen>)
                (menu, inventory, title) -> new UpgradeScreen(UpgradeType.INSERT, menu, inventory, title)
        );
        registration.register(
            MenuTypes.INSTANCE.getExportUpgrade(),
            (com.refinedmods.refinedstorage.common.AbstractClientModInitializer.ScreenConstructor<
                UpgradeContainerMenu, UpgradeScreen
            >) (menu, inventory, title) -> new UpgradeScreen(UpgradeType.EXPORT, menu, inventory, title)
        );
    }
}
