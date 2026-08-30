package com.ultramega.rsinsertexportupgrade.common;

public interface Config {
    UpgradeEntry getUpgrade();

    interface UpgradeEntry {
        long getInsertUpgradeEnergyUsage();

        long getExportUpgradeEnergyUsage();
    }
}
