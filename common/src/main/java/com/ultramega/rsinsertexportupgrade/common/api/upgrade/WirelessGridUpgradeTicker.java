package com.ultramega.rsinsertexportupgrade.common.api.upgrade;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/** Server-side behavior for an installed wireless grid upgrade */
@API(status = Status.STABLE)
@FunctionalInterface
public interface WirelessGridUpgradeTicker {
    WirelessGridUpgradeTicker NONE = context -> false;

    /**
     * Called once per server player tick while the grid is active and its network resolves.
     * Check the relevant network permissions before transferring resources and drain energy
     * through {@link WirelessGridUpgradeContext#networkItemContext()} after successful work.
     *
     * @return whether player inventory/menu contents changed and need broadcasting
     */
    boolean tick(WirelessGridUpgradeContext context);
}
