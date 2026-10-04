package com.ultramega.refinedwirelessupgrades.common.registry;

import com.ultramega.refinedwirelessupgrades.common.menu.MagnetContainerMenu;
import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeContainerMenu;

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
    @Nullable
    private Supplier<MenuType<MagnetContainerMenu>> magnetUpgrade;

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

    public MenuType<MagnetContainerMenu> getMagnetUpgrade() {
        return requireNonNull(this.magnetUpgrade).get();
    }

    public void setMagnetUpgrade(final Supplier<MenuType<MagnetContainerMenu>> supplier) {
        this.magnetUpgrade = supplier;
    }
}
