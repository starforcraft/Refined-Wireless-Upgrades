package com.ultramega.refinedwirelessupgrades.common.registry;

import com.ultramega.refinedwirelessupgrades.common.item.BlockPickerUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.item.ExportUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.item.InsertUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.item.MagnetUpgradeState;
import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridUpgradeState;

import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class ModDataComponents {
    public static final ModDataComponents INSTANCE = new ModDataComponents();

    @Nullable
    private Supplier<DataComponentType<WirelessGridUpgradeState>> gridUpgrades;
    @Nullable
    private Supplier<DataComponentType<InsertUpgradeState>> insertUpgradeState;
    @Nullable
    private Supplier<DataComponentType<ExportUpgradeState>> exportUpgradeState;
    @Nullable
    private Supplier<DataComponentType<BlockPickerUpgradeState>> blockPickerUpgradeState;
    @Nullable
    private Supplier<DataComponentType<MagnetUpgradeState>> magnetUpgradeState;

    private ModDataComponents() {
    }

    public DataComponentType<WirelessGridUpgradeState> getGridUpgrades() {
        return requireNonNull(this.gridUpgrades).get();
    }

    public void setGridUpgrades(final Supplier<DataComponentType<WirelessGridUpgradeState>> supplier) {
        this.gridUpgrades = supplier;
    }

    public DataComponentType<InsertUpgradeState> getInsertUpgradeState() {
        return requireNonNull(this.insertUpgradeState).get();
    }

    public void setInsertUpgradeState(final Supplier<DataComponentType<InsertUpgradeState>> supplier) {
        this.insertUpgradeState = supplier;
    }

    public DataComponentType<ExportUpgradeState> getExportUpgradeState() {
        return requireNonNull(this.exportUpgradeState).get();
    }

    public void setExportUpgradeState(final Supplier<DataComponentType<ExportUpgradeState>> supplier) {
        this.exportUpgradeState = supplier;
    }

    public DataComponentType<BlockPickerUpgradeState> getBlockPickerUpgradeState() {
        return requireNonNull(this.blockPickerUpgradeState).get();
    }

    public void setBlockPickerUpgradeState(final Supplier<DataComponentType<BlockPickerUpgradeState>> supplier) {
        this.blockPickerUpgradeState = supplier;
    }

    public DataComponentType<MagnetUpgradeState> getMagnetUpgradeState() {
        return requireNonNull(this.magnetUpgradeState).get();
    }

    public void setMagnetUpgradeState(final Supplier<DataComponentType<MagnetUpgradeState>> supplier) {
        this.magnetUpgradeState = supplier;
    }
}
