package com.ultramega.rsinsertexportupgrade.neoforge;

import com.ultramega.rsinsertexportupgrade.common.Config;
import com.ultramega.rsinsertexportupgrade.common.DefaultValues;

import net.neoforged.neoforge.common.ModConfigSpec;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportTranslationKey;

public class ConfigImpl implements Config {
    private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
    private final ModConfigSpec spec;

    private final UpgradeEntry upgrade;

    public ConfigImpl() {
        this.upgrade = new UpgradeEntryImpl("upgrade");
        this.spec = this.builder.build();
    }

    public ModConfigSpec getSpec() {
        return this.spec;
    }

    @Override
    public UpgradeEntry getUpgrade() {
        return this.upgrade;
    }

    private static String translationKey(final String value) {
        return createInsertExportTranslationKey("text.autoconfig", "option." + value);
    }

    private class UpgradeEntryImpl implements UpgradeEntry {
        private final ModConfigSpec.LongValue insertUpgradeEnergyUsage;
        private final ModConfigSpec.LongValue exportUpgradeEnergyUsage;
        private final ModConfigSpec.LongValue blockPickerUpgradeEnergyUsage;
        private final ModConfigSpec.LongValue magnetUpgradeEnergyUsage;
        private final ModConfigSpec.DoubleValue magnetUpgradeRange;
        private final ModConfigSpec.LongValue energyCapacityUpgradeCapacity;

        UpgradeEntryImpl(final String name) {
            ConfigImpl.this.builder.translation(translationKey(name)).push(name);

            this.insertUpgradeEnergyUsage = ConfigImpl.this.builder
                .translation(translationKey(name + ".insertUpgradeEnergyUsage"))
                .defineInRange("insertUpgradeEnergyUsage", DefaultValues.INSERT_UPGRADE_ENERGY_USAGE, 0, Long.MAX_VALUE);

            this.exportUpgradeEnergyUsage = ConfigImpl.this.builder
                .translation(translationKey(name + ".exportUpgradeEnergyUsage"))
                .defineInRange("exportUpgradeEnergyUsage", DefaultValues.EXPORT_UPGRADE_ENERGY_USAGE, 0, Long.MAX_VALUE);

            this.blockPickerUpgradeEnergyUsage = ConfigImpl.this.builder
                .translation(translationKey(name + ".blockPickerUpgradeEnergyUsage"))
                .defineInRange("blockPickerUpgradeEnergyUsage", DefaultValues.BLOCK_PICKER_UPGRADE_ENERGY_USAGE, 0, Long.MAX_VALUE);

            this.magnetUpgradeEnergyUsage = ConfigImpl.this.builder
                .translation(translationKey(name + ".magnetUpgradeEnergyUsage"))
                .defineInRange("magnetUpgradeEnergyUsage", DefaultValues.MAGNET_UPGRADE_ENERGY_USAGE, 0, Long.MAX_VALUE);
            this.magnetUpgradeRange = ConfigImpl.this.builder
                .translation(translationKey(name + ".magnetUpgradeRange"))
                .defineInRange("magnetUpgradeRange", DefaultValues.MAGNET_UPGRADE_RANGE, 1.0, 32.0);

            this.energyCapacityUpgradeCapacity = ConfigImpl.this.builder
                .translation(translationKey(name + ".energyCapacityUpgradeCapacity"))
                .defineInRange("energyCapacityUpgradeCapacity", DefaultValues.ENERGY_CAPACITY_UPGRADE_CAPACITY, 0, Long.MAX_VALUE);

            ConfigImpl.this.builder.pop();
        }

        @Override
        public long getEnergyCapacityUpgradeCapacity() {
            return this.energyCapacityUpgradeCapacity.get();
        }

        @Override
        public long getMagnetUpgradeEnergyUsage() {
            return this.magnetUpgradeEnergyUsage.get();
        }

        @Override
        public double getMagnetUpgradeRange() {
            return this.magnetUpgradeRange.get();
        }

        @Override
        public long getInsertUpgradeEnergyUsage() {
            return this.insertUpgradeEnergyUsage.get();
        }

        @Override
        public long getExportUpgradeEnergyUsage() {
            return this.exportUpgradeEnergyUsage.get();
        }

        @Override
        public long getBlockPickerUpgradeEnergyUsage() {
            return this.blockPickerUpgradeEnergyUsage.get();
        }
    }
}
