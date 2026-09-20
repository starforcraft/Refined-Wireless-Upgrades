package com.ultramega.rsinsertexportupgrade.common.util;

import java.util.Arrays;
import java.util.Optional;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum UpgradeType {
    INSERT(0, "insert"),
    EXPORT(1, "export"),
    MAGNET(2, "magnet");

    public static final StreamCodec<ByteBuf, UpgradeType> STREAM_CODEC = ByteBufCodecs.idMapper(
        id -> UpgradeType.valueOf(id).orElseThrow(),
        UpgradeType::getId
    );

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
