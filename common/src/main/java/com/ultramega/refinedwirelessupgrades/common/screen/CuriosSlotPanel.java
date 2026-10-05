package com.ultramega.refinedwirelessupgrades.common.screen;

import com.ultramega.refinedwirelessupgrades.common.compat.curios.CuriosBridge;
import com.ultramega.refinedwirelessupgrades.common.menu.UpgradeContainerMenu;
import com.ultramega.refinedwirelessupgrades.common.network.CurioSlotUpdatePayload;
import com.ultramega.refinedwirelessupgrades.common.util.UpgradeType;

import com.refinedmods.refinedstorage.common.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class CuriosSlotPanel {
    private static final Identifier INVENTORY = Identifier.fromNamespaceAndPath("curios", "textures/gui/curios/inventory.png");
    private static final int MAX_ROWS = 8;
    private static final int MAX_PAGE_SIZE = 48;
    private static final int SCREEN_GAP = 2;

    private final UpgradeContainerMenu menu;
    private final UpgradeType type;

    private List<CuriosBridge.Slot> slots = List.of();

    private int pageSize = MAX_PAGE_SIZE;
    private int columns = 1;
    private int panelWidth;
    private int panelHeight;
    private int slotTop;
    private int visibleSlots;
    private boolean open;
    private int page;
    private int x;
    private int y;

    public CuriosSlotPanel(final UpgradeContainerMenu menu, final UpgradeType type) {
        this.menu = menu;
        this.type = type;
    }

    public void render(final GuiGraphicsExtractor graphics, final Font font, final int left, final int top, final int mouseX, final int mouseY) {
        this.visibleSlots = 0;
        if (!this.open || Minecraft.getInstance().player == null) {
            return;
        }
        this.slots = CuriosBridge.getSlots(Minecraft.getInstance().player);
        if (this.slots.isEmpty()) {
            return;
        }

        final int availableColumns = Math.max(1, (left - SCREEN_GAP - 14) / 18);
        this.pageSize = Math.min(MAX_PAGE_SIZE, availableColumns * MAX_ROWS);
        this.page = Math.min(this.page, this.lastPage());
        this.visibleSlots = Math.min(this.pageSize, this.slots.size() - this.page * this.pageSize);
        this.columns = Math.max(1, (this.visibleSlots + MAX_ROWS - 1) / MAX_ROWS);
        final int rows = Math.max(1, (this.visibleSlots + this.columns - 1) / this.columns);
        this.panelWidth = 14 + this.columns * 18;
        this.slotTop = this.lastPage() > 0 ? 12 : 0;
        this.panelHeight = this.slotTop + rows * 18 + 14;
        this.x = left - SCREEN_GAP - this.panelWidth;
        this.y = Math.clamp(top, 2, Math.max(2, Minecraft.getInstance().getWindow().getGuiScaledHeight() - this.panelHeight - 2));

        this.drawSlotColumns(graphics);
        for (int cell = 0; cell < this.visibleSlots; ++cell) {
            final int index = this.page * this.pageSize + cell;
            if (index >= this.slots.size()) {
                break;
            }
            final CuriosBridge.Slot slot = this.slots.get(index);
            final int sx = this.x + 8 + cell % this.columns * 18;
            final int sy = this.y + this.slotTop + 8 + cell / this.columns * 18;
            if (slot.stack().isEmpty()) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, slot.icon(), sx, sy, 16, 16);
            } else {
                graphics.item(slot.stack(), sx, sy);
            }
            final int filter = this.menu.getSelectedCurioSlots().getOrDefault(slot.key(), 0);
            UpgradeScreen.renderSlotHighlight(graphics, this.type, font, sx, sy, filter > 0, filter);
            if (mouseX >= sx && mouseX < sx + 16 && mouseY >= sy && mouseY < sy + 16) {
                graphics.fill(sx, sy, sx + 16, sy + 16, 0x80FFFFFF);
            }
        }
        if (this.lastPage() > 0) {
            this.drawPageButton(graphics, false, mouseX, mouseY);
            this.drawPageButton(graphics, true, mouseX, mouseY);
        }
    }

    public boolean mouseClicked(final double mouseX, final double mouseY, final int button, final boolean carrying) {
        if (!this.contains(mouseX, mouseY) || carrying || (button != 0 && button != 1)) {
            return false;
        }
        if (this.lastPage() > 0 && button == 0) {
            if (this.overPageButton(mouseX, mouseY, false) && this.page > 0) {
                --this.page;
                return true;
            }
            if (this.overPageButton(mouseX, mouseY, true) && this.page < this.lastPage()) {
                ++this.page;
                return true;
            }
        }

        final int rx = (int) mouseX - this.x - 8;
        final int ry = (int) mouseY - this.y - this.slotTop - 8;
        if (rx >= 0 && ry >= 0 && rx < this.columns * 18 && rx % 18 < 16 && ry % 18 < 16) {
            final int cell = ry / 18 * this.columns + rx / 18;
            final int index = this.page * this.pageSize + cell;
            if (cell < this.visibleSlots && index < this.slots.size()) {
                final String key = this.slots.get(index).key();
                final var selected = new HashMap<>(this.menu.getSelectedCurioSlots());
                final int current = selected.getOrDefault(key, 0);
                final int maximum = this.type == UpgradeType.EXPORT ? UpgradeContainerMenu.FILTER_SLOT_COUNT : 1;
                final int filter = button == 1 || current >= maximum ? 0 : current + 1;
                if (filter == 0) {
                    selected.remove(key);
                } else {
                    selected.put(key, filter);
                }
                this.menu.receiveSelectedCurioSlots(selected);
                Platform.INSTANCE.sendPacketToServer(new CurioSlotUpdatePayload(this.menu.containerId, key, filter));
                return true;
            }
        }

        return false;
    }

    private void drawSlotColumns(final GuiGraphicsExtractor graphics) {
        for (int cell = 0; cell < this.visibleSlots; ++cell) {
            final int column = cell % this.columns;
            final int row = cell / this.columns;
            final int rowColumns = Math.min(this.columns, this.visibleSlots - row * this.columns);
            final boolean rightEdge = column == rowColumns - 1;
            final boolean bottomEdge = cell + this.columns >= this.visibleSlots;
            final int sx = this.x + column * 18 + (column == 0 ? 0 : 7);
            final int sy = this.y + this.slotTop + row * 18 + (row == 0 ? 0 : 7);

            // Curios slots have a seven-pixel outer border and an 18-pixel cell
            final int u = column == 0 ? 0 : 7;
            final int v = row == 0 ? 0 : 7;
            final int width = column == 0 ? 25 : 18;
            final int height = row == 0 ? 25 : 18;
            this.blit(graphics, sx, sy, u, v, width, height);
            if (rightEdge) {
                this.blit(graphics, sx + width, sy, 25, v, 7, height);
            }
            if (bottomEdge) {
                // Slot strip ends at y=158; its bottom border starts at y=151
                this.blit(graphics, sx, sy + height, u, 151, width, 7);
                if (rightEdge) {
                    this.blit(graphics, sx + width, sy + height, 25, 151, 7, 7);
                }
            }
        }
    }

    ImageButton createButton(final int left, final int top) {
        final WidgetSprites sprites = new WidgetSprites(
            Identifier.fromNamespaceAndPath("curios", "button"),
            Identifier.fromNamespaceAndPath("curios", "button_highlighted")
        );
        return new ImageButton(left + 4, top + 57, 10, 10, sprites, ignored -> this.open = !this.open) {
            @Override
            public void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTicks) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprites.get(this.isActive(), this.isHovered()),
                    this.getX(), this.getY(), this.getWidth(), this.getHeight());
            }
        };
    }

    public boolean contains(final double mouseX, final double mouseY) {
        if (!this.open || this.visibleSlots == 0) {
            return false;
        }
        if (this.lastPage() > 0 && (this.overPageButton(mouseX, mouseY, false) || this.overPageButton(mouseX, mouseY, true))) {
            return true;
        }
        for (int column = 0; column < this.columns; ++column) {
            final int rows = this.rowsInColumn(column);
            final int cx = this.x + column * 18;
            if (rows > 0 && mouseX >= cx && mouseX < cx + 32
                && mouseY >= this.y + this.slotTop && mouseY < this.y + this.slotTop + rows * 18 + 14) {
                return true;
            }
        }
        return false;
    }

    public void renderTooltip(final GuiGraphicsExtractor graphics, final Font font, final int mouseX, final int mouseY) {
        if (!this.open || this.visibleSlots == 0) {
            return;
        }
        if (this.lastPage() > 0 && (this.overPageButton(mouseX, mouseY, false) || this.overPageButton(mouseX, mouseY, true))) {
            graphics.setTooltipForNextFrame(font, Component.translatable("gui.curios.page", this.page + 1, this.lastPage() + 1), mouseX, mouseY);
            return;
        }
        final int rx = mouseX - this.x - 8;
        final int ry = mouseY - this.y - this.slotTop - 8;
        if (rx >= 0 && ry >= 0 && rx < this.columns * 18 && rx % 18 < 16 && ry % 18 < 16) {
            final int cell = ry / 18 * this.columns + rx / 18;
            final int index = this.page * this.pageSize + cell;
            if (cell < this.visibleSlots && index < this.slots.size()) {
                final CuriosBridge.Slot slot = this.slots.get(index);
                final var name = Component.translatable("curios.identifier." + slot.identifier());
                final List<Component> lines = new ArrayList<>();
                lines.add(name);
                if (!slot.stack().isEmpty()) {
                    lines.add(slot.stack().getHoverName());
                }
                graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    public List<Rect2i> getExclusionZones() {
        if (!this.open || this.visibleSlots == 0) {
            return List.of();
        }
        final List<Rect2i> zones = new ArrayList<>();
        for (int column = 0; column < this.columns; ++column) {
            final int rows = this.rowsInColumn(column);
            if (rows > 0) {
                zones.add(new Rect2i(this.x + column * 18, this.y + this.slotTop, 32, rows * 18 + 14));
            }
        }
        if (this.lastPage() > 0) {
            zones.add(new Rect2i(this.pageButtonX(false), this.y, 22, 12));
        }
        return zones;
    }

    private int rowsInColumn(final int column) {
        return Math.max(0, (this.visibleSlots - column + this.columns - 1) / this.columns);
    }

    private int pageButtonX(final boolean next) {
        return this.x + this.panelWidth - (next ? 11 : 22);
    }

    private boolean overPageButton(final double mouseX, final double mouseY, final boolean next) {
        final int bx = this.pageButtonX(next);
        return mouseX >= bx && mouseX < bx + 11 && mouseY >= this.y && mouseY < this.y + 12;
    }

    private void drawPageButton(final GuiGraphicsExtractor graphics, final boolean next, final int mouseX, final int mouseY) {
        final boolean enabled = next ? this.page < this.lastPage() : this.page > 0;
        final boolean hovered = this.overPageButton(mouseX, mouseY, next);
        final int u = (next ? 43 : 32) + (enabled && hovered ? 22 : 0);
        this.blit(graphics, this.pageButtonX(next), this.y, u, enabled ? 25 : 37, 11, 12);
    }

    private void blit(final GuiGraphicsExtractor graphics, final int x, final int y, final int u, final int v, final int width, final int height) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x, y, u, v, width, height, 256, 256);
    }

    private int lastPage() {
        return Math.max(0, (this.slots.size() - 1) / this.pageSize);
    }
}
