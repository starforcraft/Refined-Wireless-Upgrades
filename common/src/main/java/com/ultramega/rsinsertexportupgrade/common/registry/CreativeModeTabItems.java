package com.ultramega.rsinsertexportupgrade.common.registry;

import java.util.function.Consumer;

import net.minecraft.world.item.ItemStack;

public final class CreativeModeTabItems {
    private CreativeModeTabItems() {
    }

    public static void appendItems(final Consumer<ItemStack> consumer) {
        consumer.accept(Items.INSTANCE.getInsertUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getExportUpgrade().getDefaultInstance());
        consumer.accept(Items.INSTANCE.getBlockPickerUpgrade().getDefaultInstance());
    }
}
