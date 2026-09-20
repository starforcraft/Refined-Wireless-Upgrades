package com.ultramega.rsinsertexportupgrade.common.api.upgrade;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/** Creates cached behavior for one installed upgrade, independently of other grids and players */
@API(status = Status.STABLE)
@FunctionalInterface
public interface WirelessGridUpgradeFactory {
    /**
     * The stack is a configuration snapshot. Changing it does not change the installed upgrade.
     * Called again when grid custom data, registry access, slot count or registrations change.
     * Return {@link WirelessGridUpgradeTicker#NONE} when no ticking is required.
     * Cache parsed configuration here, but keep persistent state in item data, not in the ticker.
     */
    WirelessGridUpgradeTicker create(ItemStack upgrade, HolderLookup.Provider registries);
}
