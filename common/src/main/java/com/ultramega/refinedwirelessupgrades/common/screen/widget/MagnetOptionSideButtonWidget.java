package com.ultramega.refinedwirelessupgrades.common.screen.widget;

import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;
import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;

public final class MagnetOptionSideButtonWidget extends AbstractSideButtonWidget {
    private final ClientProperty<Boolean> property;

    public MagnetOptionSideButtonWidget(final ClientProperty<Boolean> property) {
        super(button -> property.setValue(!property.getValue()));
        this.property = property;
    }

    @Override
    protected ResourceLocation getSprite() {
        return createInsertExportIdentifier(this.property.getValue() ? "network" : "inventory");
    }

    @Override
    protected MutableComponent getTitle() {
        return createInsertExportTranslation("gui", "magnet.destination");
    }

    @Override
    protected List<MutableComponent> getSubText() {
        final String value = this.property.getValue() ? "network" : "inventory";
        return List.of(createInsertExportTranslation("gui", "magnet.option." + value));
    }

    @Override
    protected Component getHelpText() {
        final String destination = this.property.getValue() ? "network" : "inventory";
        return createInsertExportTranslation("gui", "magnet.destination." + destination + "_help");
    }
}
