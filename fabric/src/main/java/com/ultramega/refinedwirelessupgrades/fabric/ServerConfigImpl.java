package com.ultramega.refinedwirelessupgrades.fabric;

import com.ultramega.refinedwirelessupgrades.common.ServerConfig;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.MOD_ID;

@Config(name = MOD_ID + "-server")
public final class ServerConfigImpl implements ConfigData, ServerConfig {
    @ConfigEntry.Gui.RequiresRestart
    @ConfigEntry.BoundedDiscrete(min = 1, max = MAX_WIRELESS_GRID_UPGRADE_SLOTS)
    private int wirelessGridUpgradeSlots = DEFAULT_WIRELESS_GRID_UPGRADE_SLOTS;

    public static ServerConfigImpl get() {
        return AutoConfig.getConfigHolder(ServerConfigImpl.class).getConfig();
    }

    @Override
    public int getWirelessGridUpgradeSlots() {
        return Math.clamp(this.wirelessGridUpgradeSlots, 1, MAX_WIRELESS_GRID_UPGRADE_SLOTS);
    }
}
