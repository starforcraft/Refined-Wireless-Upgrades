package com.ultramega.rsinsertexportupgrade.common.registry;

import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeContainerMenu;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.inventory.MenuType;

import static java.util.Objects.requireNonNull;

public final class MenuTypes {
    public static final MenuTypes INSTANCE = new MenuTypes();

    @Nullable
    private Supplier<MenuType<UpgradeContainerMenu>> insertUpgrade;
    @Nullable
    private Supplier<MenuType<UpgradeContainerMenu>> exportUpgrade;

    private MenuTypes() {
    }

    public MenuType<UpgradeContainerMenu> getInsertUpgrade() {
        return requireNonNull(this.insertUpgrade).get();
    }

    public void setInsertUpgrade(final Supplier<MenuType<UpgradeContainerMenu>> insertUpgrade) {
        this.insertUpgrade = insertUpgrade;
    }

    public MenuType<UpgradeContainerMenu> getExportUpgrade() {
        return requireNonNull(this.exportUpgrade).get();
    }

    public void setExportUpgrade(final Supplier<MenuType<UpgradeContainerMenu>> exportUpgrade) {
        this.exportUpgrade = exportUpgrade;
    }
}
