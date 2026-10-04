package com.ultramega.refinedwirelessupgrades.common.screen;

import com.ultramega.refinedwirelessupgrades.common.compat.curios.CuriosBridge;
import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeContainerMenu;
import com.ultramega.refinedwirelessupgrades.common.menu.UpgradePlayerSlot;
import com.ultramega.refinedwirelessupgrades.common.network.ReturnToGridPayload;
import com.ultramega.refinedwirelessupgrades.common.network.UpdateSelectedInventorySlotsPayload;
import com.ultramega.refinedwirelessupgrades.common.util.UpgradeType;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.storage.FilterModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;
import com.refinedmods.refinedstorage.common.support.widget.FuzzyModeSideButtonWidget;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;
import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;

public class UpgradeScreen extends AbstractBaseScreen<UpgradeContainerMenu> {
    private static final ResourceLocation BACKGROUND = createInsertExportIdentifier("textures/gui/upgrade.png");
    private static final ResourceLocation CHECKMARK = createInsertExportIdentifier("textures/gui/checkmark.png");
    private static final ResourceLocation XMARK = createInsertExportIdentifier("textures/gui/xmark.png");
    private static final ResourceLocation MASS_SELECT = createInsertExportIdentifier("textures/gui/mass_select.png");

    private static final int BASE_BACKGROUND_WIDTH = 191;
    private static final int BACKGROUND_WIDTH_WITH_EXTRA_SLOTS = 225;
    private static final int MASS_SELECT_X = 183;
    private static final int MASS_SELECT_INVENTORY_Y = 76;
    private static final int MASS_SELECT_HOTBAR_Y = MASS_SELECT_INVENTORY_Y + 58;

    @Nullable
    private final CuriosSlotPanel curiosPanel;
    private boolean clickedCuriosPanel;
    private final UpgradeType type;
    private final int[] selectedInventorySlots;

    private boolean cancel = false;
    private boolean dragging = false;
    private boolean pickedUpDraggedStack = false;
    private boolean suppressReleaseAfterDrag = false;
    private boolean blockedQuickCraftDrag = false;
    private int clickedSlotId = -1;
    private boolean returning;

    public UpgradeScreen(final UpgradeType type,
                         final UpgradeContainerMenu menu,
                         final Inventory playerInventory,
                         final Component title) {
        super(menu, playerInventory, title);
        this.type = type;
        this.curiosPanel = CuriosBridge.isLoaded() ? new CuriosSlotPanel(menu, type) : null;
        this.selectedInventorySlots = new int[UpgradeContainerMenu.INVENTORY_SLOT_COUNT];
        menu.setSelectedInventorySlotsListener(this::setSelectedInventorySlots);
        this.imageWidth = type == UpgradeType.EXPORT ? BACKGROUND_WIDTH_WITH_EXTRA_SLOTS : BASE_BACKGROUND_WIDTH;
        this.imageHeight = 163;
        this.titleLabelX = 23;
        this.inventoryLabelX = 23;
        this.inventoryLabelY = 69;
    }

    @Override
    protected void init() {
        super.init();
        UpgradeScreenNavigation.restoreMousePosition();
        if (this.curiosPanel != null) {
            this.addRenderableWidget(this.curiosPanel.createButton(this.leftPos, this.topPos));
        }
        if (this.type == UpgradeType.INSERT) {
            this.addSideButton(new FilterModeSideButtonWidget(
                this.getMenu().getProperty(PropertyTypes.FILTER_MODE),
                createInsertExportTranslation("gui", "filter_mode.allow_help"),
                createInsertExportTranslation("gui", "filter_mode.block_help")
            ));
        }
        this.addSideButton(new FuzzyModeSideButtonWidget(
            this.getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            () -> FuzzyModeSideButtonWidget.Type.GENERIC
        ));
    }

    @Override
    protected ResourceLocation getTexture() {
        return BACKGROUND;
    }

    @Override
    protected int getSideButtonX() {
        return super.getSideButtonX() + 15;
    }

    @Override
    protected void renderBg(final GuiGraphics graphics,
                            final float delta,
                            final int mouseX,
                            final int mouseY) {
        graphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        this.renderResourceSlots(graphics);

        for (final Slot slot : this.getMenu().slots) {
            if (slot instanceof ResourceSlot) {
                if (this.type == UpgradeType.EXPORT) {
                    renderSlotHighlight(graphics, this.type, this.font, this.leftPos + slot.x, this.topPos + slot.y, true, slot.getContainerSlot() + 1);
                }
                continue;
            }

            if (!(slot instanceof UpgradePlayerSlot)) {
                continue;
            }

            final int index = slot.getContainerSlot();
            if (index < 0 || index >= this.selectedInventorySlots.length) {
                continue;
            }

            final int selectedSlot = this.selectedInventorySlots[index];
            if (selectedSlot >= 1) {
                renderSlotHighlight(graphics, this.type, this.font, this.leftPos + slot.x, this.topPos + slot.y, true, selectedSlot);
            } else if (selectedSlot == 0) {
                renderSlotHighlight(graphics, this.type, this.font, this.leftPos + slot.x, this.topPos + slot.y, false, -1);
            }
        }

        renderMassSelect(graphics, this.leftPos + MASS_SELECT_X, this.topPos + MASS_SELECT_INVENTORY_Y);
        if (this.curiosPanel != null) {
            this.curiosPanel.render(graphics, this.font, this.leftPos, this.topPos + 67, mouseX, mouseY);
        }
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        if (this.curiosPanel != null) {
            this.curiosPanel.renderTooltip(graphics, this.font, mouseX, mouseY);
        }
    }

    public static void renderSlotHighlight(final GuiGraphics graphics,
                                           final UpgradeType type,
                                           final Font font,
                                           final int x,
                                           final int y,
                                           final boolean checked,
                                           final int filterIndex) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300.0F);

        if (checked) {
            if (type == UpgradeType.INSERT) {
                graphics.blit(CHECKMARK, x, y, 0, 0, 16, 16, 16, 16);
            } else {
                graphics.pose().pushPose();
                graphics.pose().scale(0.5F, 0.5F, 1.0F);

                final String text = String.valueOf(filterIndex);
                graphics.drawString(font, text, (x + 16) * 2 - font.width(text), y * 2, Color.GREEN.hashCode());

                graphics.pose().popPose();
            }
        } else {
            graphics.blit(XMARK, x, y, 0, 0, 16, 16, 16, 16);
        }

        graphics.pose().popPose();
    }

    public static void renderMassSelect(final GuiGraphics graphics, final int x, final int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300.0F);

        graphics.blit(MASS_SELECT, x, y, 0, 0, 16, 16, 16, 16);
        graphics.blit(MASS_SELECT, x, y + (16 * 3) + 10, 0, 0, 16, 16, 16, 16);

        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        final ItemStack carried = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
        if (this.curiosPanel != null && this.curiosPanel.contains(mouseX, mouseY)) {
            this.clickedCuriosPanel = true;
            if (this.curiosPanel.mouseClicked(mouseX, mouseY, button, !carried.isEmpty())) {
                this.playClickSound();
            }
            return true;
        }
        if (carried.isEmpty()) {
            final Slot slot = this.findSlot(mouseX, mouseY);
            if (slot instanceof UpgradePlayerSlot) {
                // Let vanilla handle shift-clicks normally
                if (hasShiftDown() && !slot.getItem().isEmpty()) {
                    return super.mouseClicked(mouseX, mouseY, button);
                }

                this.cancel = false;
                this.dragging = false;
                this.pickedUpDraggedStack = false;
                this.clickedSlotId = slot.index;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(final double mouseX,
                                final double mouseY,
                                final int button,
                                final double dragX,
                                final double dragY) {
        if (this.clickedCuriosPanel) {
            return true;
        }
        this.dragging = true;

        final ItemStack carried = this.draggingItem;
        if (!carried.isEmpty()) {
            this.clearDraggingState();
            this.blockedQuickCraftDrag = true;
            this.suppressReleaseAfterDrag = true;
            return true;
        }

        if (this.clickedSlotId != -1 && !this.pickedUpDraggedStack) {
            this.slotClicked(this.menu.slots.get(this.clickedSlotId), this.clickedSlotId, button, ClickType.PICKUP);
            this.pickedUpDraggedStack = true;
            this.suppressReleaseAfterDrag = true;
            this.clearDraggingState();
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        if (this.clickedCuriosPanel) {
            this.clickedCuriosPanel = false;
            return true;
        }
        boolean handled = false;

        final Slot slot = this.findSlot(mouseX, mouseY);
        final boolean suppressThisRelease = this.suppressReleaseAfterDrag || this.pickedUpDraggedStack || this.blockedQuickCraftDrag;
        if (!suppressThisRelease && !this.cancel && !this.dragging && slot instanceof UpgradePlayerSlot && slot.index == this.clickedSlotId) {
            if (this.draggingItem.isEmpty()) {
                final int slotId = slot.getContainerSlot();
                if (slotId >= 0 && slotId < this.selectedInventorySlots.length) {
                    if (button == 0) {
                        this.increaseSelectedInventorySlot(slotId);
                        handled = true;
                    } else if (button == 1) {
                        this.selectedInventorySlots[slotId] = 0;
                        handled = true;
                    }

                    if (handled) {
                        this.sendUpdate();
                        this.playClickSound();
                    }
                }
            }
        }

        this.clearDraggingState();

        this.cancel = false;
        this.dragging = false;
        this.pickedUpDraggedStack = false;
        this.suppressReleaseAfterDrag = false;
        this.blockedQuickCraftDrag = false;
        this.clickedSlotId = -1;

        // If the user dragged an item, consume the release
        // This keeps the item on the cursor and requires a new click
        if (suppressThisRelease) {
            return true;
        }

        // Check mass select buttons
        final boolean clickedInv = this.isHovering(MASS_SELECT_X + 1, MASS_SELECT_INVENTORY_Y + 1, 4, 5, mouseX, mouseY);
        final boolean clickedHotbar = this.isHovering(MASS_SELECT_X + 1, MASS_SELECT_HOTBAR_Y + 1, 4, 5, mouseX, mouseY);

        if (clickedInv || clickedHotbar) {
            final int start = clickedHotbar ? 0 : 9;
            final int end = clickedHotbar ? 9 : UpgradeContainerMenu.PLAYER_INVENTORY_SLOT_COUNT;

            for (int i = start; i < end; i++) {
                if (button == 0) {
                    this.increaseSelectedInventorySlot(i);
                } else if (button == 1) {
                    this.selectedInventorySlots[i] = 0;
                }
            }

            this.sendUpdate();
            this.playClickSound();

            handled = true;
        }

        return handled || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public List<Rect2i> getExclusionZones() {
        final List<Rect2i> zones = new ArrayList<>(super.getExclusionZones());
        if (this.curiosPanel != null) {
            zones.addAll(this.curiosPanel.getExclusionZones());
        }
        return zones;
    }

    @Override
    public void onClose() {
        if (this.returning) {
            return;
        }
        this.returning = true;
        this.sendUpdate();
        UpgradeScreenNavigation.rememberMousePosition();
        Platform.INSTANCE.sendPacketToServer(new ReturnToGridPayload(this.getMenu().containerId));
    }

    private void increaseSelectedInventorySlot(final int index) {
        if (this.type == UpgradeType.EXPORT) {
            if (this.selectedInventorySlots[index] >= UpgradeContainerMenu.FILTER_SLOT_COUNT) {
                this.selectedInventorySlots[index] = 0;
            } else {
                this.selectedInventorySlots[index] += 1;
            }
        } else {
            this.selectedInventorySlots[index] = this.selectedInventorySlots[index] == 0 ? 1 : 0;
        }
    }

    private void setSelectedInventorySlots(final int[] selectedInventorySlots) {
        Arrays.fill(this.selectedInventorySlots, 0);
        System.arraycopy(
            selectedInventorySlots,
            0,
            this.selectedInventorySlots,
            0,
            Math.min(this.selectedInventorySlots.length, selectedInventorySlots.length)
        );
    }

    private void sendUpdate() {
        Platform.INSTANCE.sendPacketToServer(new UpdateSelectedInventorySlotsPayload(this.getMenu().containerId, this.selectedInventorySlots));
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
