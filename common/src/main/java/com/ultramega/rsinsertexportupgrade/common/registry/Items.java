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
}
