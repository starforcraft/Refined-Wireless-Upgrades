package com.ultramega.refinedwirelessupgrades.common.util;

public final class WirelessGridUpgradeLayout {
    public static final int ROWS_PER_COLUMN = 6;
    public static final int COLUMN_WIDTH = 30;
    public static final int ROW_HEIGHT = 18;
    public static final int BORDER = 5;

    public static final int SLOT_INSET_X = 7;
    public static final int FIRST_SLOT_X = 204;
    public static final int FIRST_SLOT_Y = 6;

    private WirelessGridUpgradeLayout() {
    }

    public static int slotX(final int slot) {
        return FIRST_SLOT_X + slot / ROWS_PER_COLUMN * COLUMN_WIDTH;
    }

    public static int slotY(final int slot) {
        return FIRST_SLOT_Y + slot % ROWS_PER_COLUMN * ROW_HEIGHT;
    }

    public static int columns(final int count) {
        return (count + ROWS_PER_COLUMN - 1) / ROWS_PER_COLUMN;
    }

    public static int rows(final int count, final int column) {
        return Math.clamp(count - (long) column * ROWS_PER_COLUMN, 0, ROWS_PER_COLUMN);
    }

    public static int height(final int count, final int column) {
        return BORDER * 2 + rows(count, column) * ROW_HEIGHT;
    }
}
