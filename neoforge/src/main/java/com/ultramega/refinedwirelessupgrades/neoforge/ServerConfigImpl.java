package com.ultramega.refinedwirelessupgrades.neoforge;

import com.ultramega.refinedwirelessupgrades.common.ServerConfig;

import net.neoforged.neoforge.common.ModConfigSpec;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportTranslationKey;

public final class ServerConfigImpl implements ServerConfig {
    private final ModConfigSpec.IntValue wirelessGridUpgradeSlots;
    private final ModConfigSpec spec;

    public ServerConfigImpl() {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        this.wirelessGridUpgradeSlots = builder
            .translation(translationKey("wirelessGridUpgradeSlots"))
            .worldRestart()
            .defineInRange("wirelessGridUpgradeSlots", DEFAULT_WIRELESS_GRID_UPGRADE_SLOTS, 1, MAX_WIRELESS_GRID_UPGRADE_SLOTS);
        this.spec = builder.build();
    }

    public ModConfigSpec getSpec() {
        return this.spec;
    }

    @Override
    public int getWirelessGridUpgradeSlots() {
        return this.wirelessGridUpgradeSlots.get();
    }

    private static String translationKey(final String value) {
        return createInsertExportTranslationKey("text.autoconfig", "option." + value);
    }
}
