package com.ultramega.refinedwirelessupgrades.common.registry;

import net.minecraft.resources.Identifier;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public final class ContentIds {
    public static final Identifier INSERT_UPGRADE = createInsertExportIdentifier("insert_upgrade");
    public static final Identifier EXPORT_UPGRADE = createInsertExportIdentifier("export_upgrade");
    public static final Identifier BLOCK_PICKER_UPGRADE = createInsertExportIdentifier("block_picker_upgrade");
    public static final Identifier MAGNET_UPGRADE = createInsertExportIdentifier("magnet_upgrade");
    public static final Identifier ENERGY_CAPACITY_UPGRADE = createInsertExportIdentifier("energy_capacity_upgrade");

    private ContentIds() {
    }
}
