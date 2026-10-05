package com.ultramega.refinedwirelessupgrades.common.screen.widget;

import com.ultramega.refinedwirelessupgrades.common.util.UpgradeType;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;
import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;
import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportTranslationKey;

public class UpgradeSideButtonWidget extends AbstractSideButtonWidget {
    private final UpgradeType type;

    public UpgradeSideButtonWidget(final UpgradeType type, final OnPress pressAction) {
        super(pressAction);
        this.type = type;
        this.visible = false;
    }

    @Override
    protected Identifier getSprite() {
        return createInsertExportIdentifier(this.type.getName() + "_upgrade");
    }

    @Override
    protected MutableComponent getTitle() {
        return Component.translatable(
            createInsertExportTranslationKey("sidebutton", "open_upgrade"),
            createInsertExportTranslation("item", this.type.getName() + "_upgrade")
        );
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return List.of();
    }

    @Override
    protected Component getHelpText() {
        return createInsertExportTranslation("item", this.type.getName() + "_upgrade.help");
    }
}
