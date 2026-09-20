package com.ultramega.rsinsertexportupgrade.common.network;

import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeConfiguration;
import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeContainerMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

final class PayloadStreamCodecs {
    static final StreamCodec<ByteBuf, int[]> SELECTED_INVENTORY_SLOTS = ByteBufCodecs.VAR_INT
        .apply(ByteBufCodecs.list(UpgradeContainerMenu.INVENTORY_SLOT_COUNT))
        .map(PayloadStreamCodecs::toArray, PayloadStreamCodecs::toList);

    static final StreamCodec<ByteBuf, Map<String, Integer>> SELECTED_CURIO_SLOTS = ByteBufCodecs.map(
        HashMap::new, ByteBufCodecs.stringUtf8(256), ByteBufCodecs.VAR_INT, UpgradeConfiguration.MAX_CURIO_SELECTIONS
    );

    private PayloadStreamCodecs() {
    }

    private static int[] toArray(final List<Integer> values) {
        final int[] result = new int[values.size()];
        for (int i = 0; i < values.size(); ++i) {
            result[i] = values.get(i);
        }
        return result;
    }

    private static List<Integer> toList(final int[] values) {
        final List<Integer> result = new ArrayList<>(values.length);
        for (final int value : values) {
            result.add(value);
        }
        return result;
    }
}
