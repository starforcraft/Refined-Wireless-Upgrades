package com.ultramega.refinedwirelessupgrades.common.util;

import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;

import java.util.function.Consumer;

import net.minecraft.world.Container;
import org.jspecify.annotations.Nullable;

public final class ObservableUpgradeContainer extends UpgradeContainer {
    private @Nullable Consumer<Container> changeListener;

    public ObservableUpgradeContainer(final UpgradeDestination destination, final int size) {
        super(destination, size);
    }

    public void setChangeListener(final Consumer<Container> listener) {
        this.changeListener = listener;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.changeListener != null) {
            this.changeListener.accept(this);
        }
    }
}
