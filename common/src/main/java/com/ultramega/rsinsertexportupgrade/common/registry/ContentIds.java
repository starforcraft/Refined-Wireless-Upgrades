package com.ultramega.rsinsertexportupgrade.common.registry;

import net.minecraft.resources.ResourceLocation;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportIdentifier;

public final class ContentIds {
    public static final ResourceLocation INSERT_UPGRADE = createInsertExportIdentifier("insert_upgrade");
    public static final ResourceLocation EXPORT_UPGRADE = createInsertExportIdentifier("export_upgrade");
    public static final ResourceLocation BLOCK_PICKER_UPGRADE = createInsertExportIdentifier("block_picker_upgrade");

    private ContentIds() {
    }
}
