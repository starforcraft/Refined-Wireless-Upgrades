package com.ultramega.rsinsertexportupgrade.common.util;

import java.util.Arrays;
import java.util.Optional;

public enum UpgradeType {
    INSERT(0, "insert"),
    EXPORT(1, "export");

    private final int id;
    private final String name;

    UpgradeType(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public static Optional<UpgradeType> valueOf(final int value) {
        return Arrays.stream(values())
            .filter(legNo -> legNo.id == value)
            .findFirst();
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
