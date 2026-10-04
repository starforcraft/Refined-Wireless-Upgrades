package com.ultramega.refinedwirelessupgrades.common.mixin;

import com.ultramega.refinedwirelessupgrades.common.api.client.WirelessGridSideButtonContext;
import com.ultramega.refinedwirelessupgrades.common.api.client.WirelessGridSideButtonRegistry;
import com.ultramega.refinedwirelessupgrades.common.screen.UpgradeScreenNavigation;
import com.ultramega.refinedwirelessupgrades.common.util.GridSlotReferenceAccessor;
import com.ultramega.refinedwirelessupgrades.common.util.IGridUpgrade;
import com.ultramega.refinedwirelessupgrades.common.util.UpgradeSlotsExtraAreaProvider;
import com.ultramega.refinedwirelessupgrades.common.util.WirelessGridUpgradeLayout;

import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.screen.AbstractGridScreen;
import com.refinedmods.refinedstorage.common.support.stretching.AbstractStretchingScreen;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

@Mixin(AbstractGridScreen.class)
public abstract class AbstractGridScreenMixin<T extends AbstractGridContainerMenu> extends AbstractStretchingScreen<T> implements UpgradeSlotsExtraAreaProvider {
    @Unique
    private static final ResourceLocation UPGRADE_SLOTS = createInsertExportIdentifier("textures/gui/sprites/upgrade_slots.png");

    @Unique
    private static final int UPGRADE_SLOTS_TEXTURE_HEIGHT = 46;

    @Shadow
    @Final
    private Inventory playerInventory;

    @Unique
    private final Map<Item, AbstractSideButtonWidget> wirelessUpgrades$sideButtons = new HashMap<>();

    @Unique
    private final List<Rect2i> wirelessUpgrades$sideButtonExclusionZones = new ArrayList<>();

    @Unique
    private boolean wirelessUpgrades$sideButtonsInitialized;

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

        if (!this.wirelessUpgrades$sideButtonsInitialized) {
            this.wirelessUpgrades$refreshSideButtons();
            this.wirelessUpgrades$sideButtonsInitialized = true;
        }

        final int count = ((GridSlotReferenceAccessor) this.getMenu()).wirelessUpgrades$getUpgradeSlotCount();
        for (int column = 0; column < WirelessGridUpgradeLayout.columns(count); ++column) {
            final int panelX = x + WirelessGridUpgradeLayout.FIRST_SLOT_X - WirelessGridUpgradeLayout.SLOT_INSET_X + column * WirelessGridUpgradeLayout.COLUMN_WIDTH;
            final int panelY = y - TOP_HEIGHT;
            final int slotRows = WirelessGridUpgradeLayout.rows(count, column);
            this.wirelessUpgrades$blitUpgradeStrip(graphics, panelX, panelY, 0, WirelessGridUpgradeLayout.BORDER);
            for (int row = 0; row < slotRows; ++row) {
                this.wirelessUpgrades$blitUpgradeStrip(graphics, panelX,
                    panelY + WirelessGridUpgradeLayout.BORDER + row * WirelessGridUpgradeLayout.ROW_HEIGHT,
                    WirelessGridUpgradeLayout.BORDER, WirelessGridUpgradeLayout.ROW_HEIGHT);
            }
            this.wirelessUpgrades$blitUpgradeStrip(graphics, panelX,
                panelY + WirelessGridUpgradeLayout.BORDER + slotRows * WirelessGridUpgradeLayout.ROW_HEIGHT,
                UPGRADE_SLOTS_TEXTURE_HEIGHT - WirelessGridUpgradeLayout.BORDER, WirelessGridUpgradeLayout.BORDER);
        }
    }

    @Unique
    private void wirelessUpgrades$blitUpgradeStrip(final GuiGraphics graphics, final int x, final int y, final int sourceY, final int height) {
        graphics.blit(UPGRADE_SLOTS, x, y, 0, sourceY, WirelessGridUpgradeLayout.COLUMN_WIDTH, height,
            WirelessGridUpgradeLayout.COLUMN_WIDTH, UPGRADE_SLOTS_TEXTURE_HEIGHT);
    }

    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private void init(final int rows, final CallbackInfo ci) {
        if (!(this.getMenu() instanceof IGridUpgrade)) {
            return;
        }

        UpgradeScreenNavigation.restoreMousePosition();

        this.wirelessUpgrades$sideButtons.clear();
        this.wirelessUpgrades$removeUpgradeSideButtonExclusionZones();
        this.wirelessUpgrades$sideButtonsInitialized = false;
    }

    @Inject(method = "containerTick", at = @At("TAIL"), remap = false)
    private void containerTick(final CallbackInfo ci) {
        this.wirelessUpgrades$refreshSideButtons();
        this.wirelessUpgrades$sideButtonsInitialized = true;
    }

    @Unique
    private void wirelessUpgrades$refreshSideButtons() {
        if (!(this.getMenu() instanceof IGridUpgrade)) {
            return;
        }
        for (final WirelessGridSideButtonRegistry.Entry entry : WirelessGridSideButtonRegistry.getEntries()) {
            final WirelessGridSideButtonContext context = new WirelessGridSideButtonContext((AbstractGridScreen<?>) (Object) this, this.playerInventory, entry.upgrade());
            final AbstractSideButtonWidget existing = this.wirelessUpgrades$sideButtons.get(entry.upgrade());
            if (context.getUpgradeSlot() >= 0 && existing == null) {
                final AbstractSideButtonWidget button = entry.create(context);
                button.visible = true;
                this.wirelessUpgrades$sideButtons.put(entry.upgrade(), button);
                this.addRenderableWidget(button);
            } else if (context.getUpgradeSlot() < 0 && existing != null) {
                this.removeWidget(existing);
                this.wirelessUpgrades$sideButtons.remove(entry.upgrade());
            }
        }
        this.wirelessUpgrades$layoutSideButtons();
    }

    @Unique
    private void wirelessUpgrades$layoutSideButtons() {
        this.wirelessUpgrades$removeUpgradeSideButtonExclusionZones();
        int nextY = this.topPos + this.getSideButtonY();
        for (final GuiEventListener child : this.children()) {
            if (child instanceof AbstractSideButtonWidget sideButton
                && !this.wirelessUpgrades$sideButtons.containsValue(sideButton) && sideButton.visible) {
                nextY = Math.max(nextY, sideButton.getY() + sideButton.getHeight() + 2);
            }
        }
        for (final WirelessGridSideButtonRegistry.Entry entry : WirelessGridSideButtonRegistry.getEntries()) {
            final AbstractSideButtonWidget button = this.wirelessUpgrades$sideButtons.get(entry.upgrade());
            if (button != null && button.visible) {
                button.setX(this.getSideButtonX());
                button.setY(nextY);
                final Rect2i zone = new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight());
                this.getExclusionZones().add(zone);
                this.wirelessUpgrades$sideButtonExclusionZones.add(zone);
                nextY += button.getHeight() + 2;
            }
        }
    }

    @Unique
    private void wirelessUpgrades$removeUpgradeSideButtonExclusionZones() {
        this.getExclusionZones().removeAll(this.wirelessUpgrades$sideButtonExclusionZones);
        this.wirelessUpgrades$sideButtonExclusionZones.clear();
    }

    @Override
    protected boolean hasClickedOutside(final double mouseX, final double mouseY, final int left, final int top, final int button) {
        // The upgrade panel is outside the vanilla screen bounds
        // Fabric also checks these bounds on mouse release, which otherwise drops the picked-up upgrade
        for (final Rect2i area : this.wirelessUpgrades$getUpgradeSlotsExtraAreas()) {
            if (mouseX >= area.getX() && mouseX < area.getX() + area.getWidth()
                && mouseY >= area.getY() && mouseY < area.getY() + area.getHeight()) {
                return false;
            }
        }
        return super.hasClickedOutside(mouseX, mouseY, left, top, button);
    }

    @Unique
    @Override
    public List<Rect2i> wirelessUpgrades$getUpgradeSlotsExtraAreas() {
        if (!(this.getMenu() instanceof IGridUpgrade)) {
            return List.of();
        }
        final int count = ((GridSlotReferenceAccessor) this.getMenu()).wirelessUpgrades$getUpgradeSlotCount();
        final List<Rect2i> areas = new ArrayList<>();
        for (int column = 0; column < WirelessGridUpgradeLayout.columns(count); ++column) {
            areas.add(new Rect2i(
                this.leftPos + WirelessGridUpgradeLayout.FIRST_SLOT_X - WirelessGridUpgradeLayout.SLOT_INSET_X + column * WirelessGridUpgradeLayout.COLUMN_WIDTH,
                this.topPos,
                WirelessGridUpgradeLayout.COLUMN_WIDTH,
                WirelessGridUpgradeLayout.height(count, column)
            ));
        }
        return areas;
    }
}
