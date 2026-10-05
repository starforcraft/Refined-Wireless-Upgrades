package com.ultramega.refinedwirelessupgrades.common.screen;

import com.ultramega.refinedwirelessupgrades.common.menu.MagnetContainerMenu;
import com.ultramega.refinedwirelessupgrades.common.network.ReturnToGridPayload;
import com.ultramega.refinedwirelessupgrades.common.screen.widget.MagnetOptionSideButtonWidget;

import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.storage.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyType;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;
import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;

public final class MagnetScreen extends AbstractBaseScreen<MagnetContainerMenu> {
    private static final Identifier TEXTURE = createInsertExportIdentifier("textures/gui/magnet_upgrade.png");
    private boolean returning;

    public MagnetScreen(final MagnetContainerMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, createInsertExportTranslation("gui", "magnet.pickup"), 176, 213);
        this.titleLabelX = 8;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 119;
    }

    @Override
    protected void init() {
        super.init();
        UpgradeScreenNavigation.restoreMousePosition();
        this.addSideButton(this.createFilterModeButton(MagnetContainerMenu.PICKUP_MODE, "pickup"));
        this.addSideButton(this.createFilterModeButton(MagnetContainerMenu.INSERT_MODE, "insert"));
        this.addSideButton(new MagnetOptionSideButtonWidget(this.menu.getProperty(MagnetContainerMenu.TO_NETWORK)));
    }

    private FilterModeSideButtonWidget createFilterModeButton(final PropertyType<FilterMode> type, final String name) {
        final String key = "magnet." + name;
        return new FilterModeSideButtonWidget(this.menu.getProperty(type),
            createInsertExportTranslation("gui", key + ".allow_help"), createInsertExportTranslation("gui", key + ".block_help")) {
            @Override
            protected MutableComponent getTitle() {
                return createInsertExportTranslation("gui", key);
            }
        };
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(this.font, createInsertExportTranslation("gui", "magnet.insert"), 8, 58, 4210752, false);
    }

    @Override
    public void onClose() {
        if (this.returning) {
            return;
        }
        this.returning = true;
        UpgradeScreenNavigation.rememberMousePosition();
        Platform.INSTANCE.sendPacketToServer(new ReturnToGridPayload(this.menu.containerId));
    }
}
