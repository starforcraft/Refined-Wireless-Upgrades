package com.ultramega.rsinsertexportupgrade.fabric;

import com.ultramega.rsinsertexportupgrade.common.DefaultEnergyUsage;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.MOD_ID;

@Config(name = MOD_ID)
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal", "CanBeFinal"})
public class ConfigImpl implements ConfigData, com.ultramega.rsinsertexportupgrade.common.Config {
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
        private long insertUpgradeEnergyUsage = DefaultEnergyUsage.INSERT_UPGRADE;
        private long exportUpgradeEnergyUsage = DefaultEnergyUsage.EXPORT_UPGRADE;
        private long blockPickerUpgradeEnergyUsage = DefaultEnergyUsage.BLOCK_PICKER_UPGRADE;

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
    }
}
