package com.ultramega.rsinsertexportupgrade.common.registry;

import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Items {
    public static final Items INSTANCE = new Items();

    @Nullable
    private Supplier<AbstractUpgradeItem> insertUpgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> exportUpgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> blockPickerUpgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> magnetUpgrade;
    @Nullable
    private Supplier<AbstractUpgradeItem> energyCapacityUpgrade;

    private Items() {
    }

    public AbstractUpgradeItem getInsertUpgrade() {
        return requireNonNull(this.insertUpgrade).get();
    }

    public void setInsertUpgrade(final Supplier<AbstractUpgradeItem> supplier) {
        this.insertUpgrade = supplier;
    }

    public AbstractUpgradeItem getExportUpgrade() {
        return requireNonNull(this.exportUpgrade).get();
    }

    public void setExportUpgrade(final Supplier<AbstractUpgradeItem> supplier) {
        this.exportUpgrade = supplier;
    }

    public AbstractUpgradeItem getBlockPickerUpgrade() {
        return requireNonNull(this.blockPickerUpgrade).get();
    }

    public void setBlockPickerUpgrade(final Supplier<AbstractUpgradeItem> supplier) {
        this.blockPickerUpgrade = supplier;
    }

    public AbstractUpgradeItem getMagnetUpgrade() {
        return requireNonNull(this.magnetUpgrade).get();
    }

    public void setMagnetUpgrade(final Supplier<AbstractUpgradeItem> supplier) {
        this.magnetUpgrade = supplier;
    }

    public AbstractUpgradeItem getEnergyCapacityUpgrade() {
        return requireNonNull(this.energyCapacityUpgrade).get();
    }

    public void setEnergyCapacityUpgrade(final Supplier<AbstractUpgradeItem> supplier) {
        this.energyCapacityUpgrade = supplier;
    }
}
