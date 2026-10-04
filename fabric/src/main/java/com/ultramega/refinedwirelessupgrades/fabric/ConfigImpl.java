package com.ultramega.refinedwirelessupgrades.fabric;

import com.ultramega.refinedwirelessupgrades.common.DefaultValues;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.MOD_ID;

@Config(name = MOD_ID)
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
public class ConfigImpl implements ConfigData, com.ultramega.refinedwirelessupgrades.common.Config {
    @ConfigEntry.Gui.CollapsibleObject
    private UpgradeEntryImpl upgrade = new UpgradeEntryImpl();

    public static ConfigImpl get() {
        return AutoConfig.getConfigHolder(ConfigImpl.class).getConfig();
    }

    @Override
    public UpgradeEntry getUpgrade() {
        return this.upgrade;
    }

    private static class UpgradeEntryImpl implements UpgradeEntry {
        private long insertUpgradeEnergyUsage = DefaultValues.INSERT_UPGRADE_ENERGY_USAGE;
        private long exportUpgradeEnergyUsage = DefaultValues.EXPORT_UPGRADE_ENERGY_USAGE;
        private long blockPickerUpgradeEnergyUsage = DefaultValues.BLOCK_PICKER_UPGRADE_ENERGY_USAGE;
        private long magnetUpgradeEnergyUsage = DefaultValues.MAGNET_UPGRADE_ENERGY_USAGE;
        private double magnetUpgradeRange = DefaultValues.MAGNET_UPGRADE_RANGE;
        private long energyCapacityUpgradeCapacity = DefaultValues.ENERGY_CAPACITY_UPGRADE_CAPACITY;

        @Override
        public long getEnergyCapacityUpgradeCapacity() {
            return Math.max(0, this.energyCapacityUpgradeCapacity);
        }

        @Override
        public long getInsertUpgradeEnergyUsage() {
            return this.insertUpgradeEnergyUsage;
        }

        @Override
        public long getExportUpgradeEnergyUsage() {
            return this.exportUpgradeEnergyUsage;
        }

        @Override
        public long getBlockPickerUpgradeEnergyUsage() {
            return this.blockPickerUpgradeEnergyUsage;
        }

        @Override
        public long getMagnetUpgradeEnergyUsage() {
            return Math.max(0, this.magnetUpgradeEnergyUsage);
        }

        @Override
        public double getMagnetUpgradeRange() {
            return Double.isFinite(this.magnetUpgradeRange) ? Math.clamp(this.magnetUpgradeRange, 1, 32) : 6;
        }
    }
}
