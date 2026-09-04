package com.ultramega.rsinsertexportupgrade.neoforge;

import com.ultramega.rsinsertexportupgrade.common.Config;
import com.ultramega.rsinsertexportupgrade.common.DefaultEnergyUsage;

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

        UpgradeEntryImpl(final String name) {
            ConfigImpl.this.builder.translation(translationKey(name)).push(name);

            this.insertUpgradeEnergyUsage = ConfigImpl.this.builder
                .translation(translationKey(name + ".insertUpgradeEnergyUsage"))
                .defineInRange("insertUpgradeEnergyUsage", DefaultEnergyUsage.INSERT_UPGRADE, 0, Long.MAX_VALUE);
            this.exportUpgradeEnergyUsage = ConfigImpl.this.builder
                .translation(translationKey(name + ".exportUpgradeEnergyUsage"))
                .defineInRange("exportUpgradeEnergyUsage", DefaultEnergyUsage.EXPORT_UPGRADE, 0, Long.MAX_VALUE);
            this.blockPickerUpgradeEnergyUsage = ConfigImpl.this.builder
                .translation(translationKey(name + ".blockPickerUpgradeEnergyUsage"))
                .defineInRange("blockPickerUpgradeEnergyUsage", DefaultEnergyUsage.BLOCK_PICKER_UPGRADE, 0, Long.MAX_VALUE);

            ConfigImpl.this.builder.pop();
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
