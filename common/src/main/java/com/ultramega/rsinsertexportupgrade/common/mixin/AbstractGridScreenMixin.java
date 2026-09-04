package com.ultramega.rsinsertexportupgrade.common.mixin;

import com.ultramega.rsinsertexportupgrade.common.menu.UpgradeConfiguration;
import com.ultramega.rsinsertexportupgrade.common.network.OpenUpgradePayload;
import com.ultramega.rsinsertexportupgrade.common.network.UpdateBlockPickerAmountPayload;
import com.ultramega.rsinsertexportupgrade.common.registry.Items;
import com.ultramega.rsinsertexportupgrade.common.screen.UpgradeScreenNavigation;
import com.ultramega.rsinsertexportupgrade.common.screen.widget.BlockPickerAmountSideButtonWidget;
import com.ultramega.rsinsertexportupgrade.common.screen.widget.UpgradeSideButtonWidget;
import com.ultramega.rsinsertexportupgrade.common.util.IGridUpgrade;
import com.ultramega.rsinsertexportupgrade.common.util.UpgradeSideButtonType;
import com.ultramega.rsinsertexportupgrade.common.util.UpgradeSlotsExtraAreaProvider;
import com.ultramega.rsinsertexportupgrade.common.util.UpgradeType;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import com.refinedmods.refinedstorage.common.support.stretching.AbstractStretchingScreen;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeSlot;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

@Mixin(AbstractGridScreen.class)
public abstract class AbstractGridScreenMixin<T extends AbstractGridContainerMenu> extends AbstractStretchingScreen<T> implements UpgradeSlotsExtraAreaProvider {
    @Unique
    private static final ResourceLocation UPGRADE_SLOTS = createInsertExportIdentifier("upgrade_slots");

    @Unique
    private static final int UPGRADE_SLOTS_X_OFFSET = 4;
    @Unique
    private static final int UPGRADE_SLOTS_WIDTH = 30;
    @Unique
    private static final int UPGRADE_SLOTS_HEIGHT = 46;

    @Shadow
    @Final
    private Inventory playerInventory;

    @Unique
    @Nullable
    private UpgradeSideButtonWidget insertExport$insertUpgradeSideButtonWidget;
    @Unique
    @Nullable
    private UpgradeSideButtonWidget insertExport$exportUpgradeSideButtonWidget;
    @Unique
    @Nullable
    private BlockPickerAmountSideButtonWidget insertExport$blockPickerAmountSideButtonWidget;

    @Unique
    @Nullable
    private Rect2i insertExport$insertUpgradeSideButtonExclusionZone;
    @Unique
    @Nullable
    private Rect2i insertExport$exportUpgradeSideButtonExclusionZone;
    @Unique
    @Nullable
    private Rect2i insertExport$blockPickerAmountSideButtonExclusionZone;

    @Unique
    private boolean insertExport$sideButtonsInitialized;

    protected AbstractGridScreenMixin(final T menu,
                                      final Inventory playerInventory,
                                      final TextMarquee title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "renderStretchingBackground", at = @At("TAIL"))
    private void renderStretchingBackground(final GuiGraphics graphics,
                                            final int x,
                                            final int y,
                                            final int rows,
                                            final CallbackInfo ci) {
        if (!(this.getMenu() instanceof IGridUpgrade)) {
            return;
        }

        if (!this.insertExport$sideButtonsInitialized) {
            this.insertExport$refreshSideButtons();
            this.insertExport$sideButtonsInitialized = true;
        }

        graphics.blitSprite(
            UPGRADE_SLOTS,
            x + this.imageWidth + UPGRADE_SLOTS_X_OFFSET,
            y - TOP_HEIGHT,
            UPGRADE_SLOTS_WIDTH,
            UPGRADE_SLOTS_HEIGHT
        );
    }

    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private void init(final int rows, final CallbackInfo ci) {
        if (!(this.getMenu() instanceof IGridUpgrade)) {
            return;
        }

        UpgradeScreenNavigation.restoreMousePosition();

        this.insertExport$insertUpgradeSideButtonWidget = null;
        this.insertExport$exportUpgradeSideButtonWidget = null;
        this.insertExport$blockPickerAmountSideButtonWidget = null;
        this.insertExport$insertUpgradeSideButtonExclusionZone = null;
        this.insertExport$exportUpgradeSideButtonExclusionZone = null;
        this.insertExport$blockPickerAmountSideButtonExclusionZone = null;
        this.insertExport$sideButtonsInitialized = false;
    }

    @Inject(method = "containerTick", at = @At("TAIL"), remap = false)
    private void containerTick(final CallbackInfo ci) {
        this.insertExport$refreshSideButtons();
        this.insertExport$sideButtonsInitialized = true;
    }

    @Unique
    private void insertExport$refreshSideButtons() {
        if (!(this.getMenu() instanceof IGridUpgrade)) {
            return;
        }
        final boolean insertUpgradeInstalled = this.insertExport$isUpgradeInstalled(Items.INSTANCE.getInsertUpgrade());
        final boolean exportUpgradeInstalled = this.insertExport$isUpgradeInstalled(Items.INSTANCE.getExportUpgrade());
        final boolean blockPickerUpgradeInstalled = this.insertExport$isUpgradeInstalled(Items.INSTANCE.getBlockPickerUpgrade());

        if (insertUpgradeInstalled && this.insertExport$insertUpgradeSideButtonWidget == null) {
            this.insertExport$insertUpgradeSideButtonWidget = new UpgradeSideButtonWidget(
                UpgradeType.INSERT,
                btn -> this.insertExport$openUpgrade(UpgradeType.INSERT)
            );
            this.insertExport$insertUpgradeSideButtonWidget.visible = true;
            this.addRenderableWidget(this.insertExport$insertUpgradeSideButtonWidget);
        } else if (!insertUpgradeInstalled && this.insertExport$insertUpgradeSideButtonWidget != null) {
            this.removeWidget(this.insertExport$insertUpgradeSideButtonWidget);
            this.insertExport$insertUpgradeSideButtonWidget = null;
        }

        if (exportUpgradeInstalled && this.insertExport$exportUpgradeSideButtonWidget == null) {
            this.insertExport$exportUpgradeSideButtonWidget = new UpgradeSideButtonWidget(
                UpgradeType.EXPORT,
                btn -> this.insertExport$openUpgrade(UpgradeType.EXPORT)
            );
            this.insertExport$exportUpgradeSideButtonWidget.visible = true;
            this.addRenderableWidget(this.insertExport$exportUpgradeSideButtonWidget);
        } else if (!exportUpgradeInstalled && this.insertExport$exportUpgradeSideButtonWidget != null) {
            this.removeWidget(this.insertExport$exportUpgradeSideButtonWidget);
            this.insertExport$exportUpgradeSideButtonWidget = null;
        }

        if (blockPickerUpgradeInstalled && this.insertExport$blockPickerAmountSideButtonWidget == null) {
            this.insertExport$blockPickerAmountSideButtonWidget = new BlockPickerAmountSideButtonWidget(
                this::insertExport$getBlockPickerAmount,
                amount -> Platform.INSTANCE.sendPacketToServer(new UpdateBlockPickerAmountPayload(
                    this.getMenu().containerId,
                    amount
                )),
                this,
                this.playerInventory
            );
            this.insertExport$blockPickerAmountSideButtonWidget.visible = true;
            this.addRenderableWidget(this.insertExport$blockPickerAmountSideButtonWidget);
        } else if (!blockPickerUpgradeInstalled && this.insertExport$blockPickerAmountSideButtonWidget != null) {
            this.removeWidget(this.insertExport$blockPickerAmountSideButtonWidget);
            this.insertExport$blockPickerAmountSideButtonWidget = null;
        }

        this.insertExport$layoutSideButtons();
    }

    @Unique
    private void insertExport$layoutSideButtons() {
        this.insertExport$removeUpgradeSideButtonExclusionZones();

        int nextY = this.topPos + this.getSideButtonY();
        for (final GuiEventListener child : this.children()) {
            if (child instanceof AbstractSideButtonWidget sideButton
                && sideButton != this.insertExport$insertUpgradeSideButtonWidget
                && sideButton != this.insertExport$exportUpgradeSideButtonWidget
                && sideButton != this.insertExport$blockPickerAmountSideButtonWidget
                && sideButton.visible) {
                nextY = Math.max(nextY, sideButton.getY() + sideButton.getHeight() + 2);
            }
        }

        if (this.insertExport$insertUpgradeSideButtonWidget != null) {
            nextY = this.insertExport$positionSideButton(
                this.insertExport$insertUpgradeSideButtonWidget,
                nextY,
                UpgradeSideButtonType.INSERT
            );
        }
        if (this.insertExport$exportUpgradeSideButtonWidget != null) {
            nextY = this.insertExport$positionSideButton(
                this.insertExport$exportUpgradeSideButtonWidget,
                nextY,
                UpgradeSideButtonType.EXPORT
            );
        }
        if (this.insertExport$blockPickerAmountSideButtonWidget != null) {
            this.insertExport$positionSideButton(
                this.insertExport$blockPickerAmountSideButtonWidget,
                nextY,
                UpgradeSideButtonType.BLOCK_PICKER_AMOUNT
            );
        }
    }

    @Unique
    private int insertExport$positionSideButton(final AbstractSideButtonWidget button,
                                                final int y,
                                                final UpgradeSideButtonType type) {
        button.setX(this.getSideButtonX());
        button.setY(y);
        final Rect2i exclusionZone = new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight());
        this.getExclusionZones().add(exclusionZone);
        switch (type) {
            case INSERT -> this.insertExport$insertUpgradeSideButtonExclusionZone = exclusionZone;
            case EXPORT -> this.insertExport$exportUpgradeSideButtonExclusionZone = exclusionZone;
            case BLOCK_PICKER_AMOUNT -> this.insertExport$blockPickerAmountSideButtonExclusionZone = exclusionZone;
        }
        return y + button.getHeight() + 2;
    }

    @Unique
    private void insertExport$removeUpgradeSideButtonExclusionZones() {
        if (this.insertExport$insertUpgradeSideButtonExclusionZone != null) {
            this.getExclusionZones().remove(this.insertExport$insertUpgradeSideButtonExclusionZone);
            this.insertExport$insertUpgradeSideButtonExclusionZone = null;
        }
        if (this.insertExport$exportUpgradeSideButtonExclusionZone != null) {
            this.getExclusionZones().remove(this.insertExport$exportUpgradeSideButtonExclusionZone);
            this.insertExport$exportUpgradeSideButtonExclusionZone = null;
        }
        if (this.insertExport$blockPickerAmountSideButtonExclusionZone != null) {
            this.getExclusionZones().remove(this.insertExport$blockPickerAmountSideButtonExclusionZone);
            this.insertExport$blockPickerAmountSideButtonExclusionZone = null;
        }
    }

    @Unique
    private void insertExport$openUpgrade(final UpgradeType type) {
        UpgradeScreenNavigation.rememberMousePosition();
        Platform.INSTANCE.sendPacketToServer(new OpenUpgradePayload(type));
    }

    @Unique
    private boolean insertExport$isUpgradeInstalled(final Item upgrade) {
        return this.getMenu().slots.stream()
            .filter(UpgradeSlot.class::isInstance)
            .anyMatch(slot -> slot.getItem().is(upgrade));
    }

    @Unique
    private int insertExport$getBlockPickerAmount() {
        return this.getMenu().slots.stream()
            .filter(UpgradeSlot.class::isInstance)
            .map(Slot::getItem)
            .filter(stack -> stack.is(Items.INSTANCE.getBlockPickerUpgrade()))
            .findFirst()
            .map(UpgradeConfiguration::getBlockPickerAmount)
            .orElse(UpgradeConfiguration.DEFAULT_BLOCK_PICKER_AMOUNT);
    }

    @Unique
    @Override
    public List<Rect2i> insertexport$getUpgradeSlotsExtraAreas() {
        if (!(this.getMenu() instanceof IGridUpgrade)) {
            return List.of();
        }
        return List.of(new Rect2i(
            this.leftPos + this.imageWidth + UPGRADE_SLOTS_X_OFFSET,
            this.topPos,
            UPGRADE_SLOTS_WIDTH,
            UPGRADE_SLOTS_HEIGHT
        ));
    }
}
