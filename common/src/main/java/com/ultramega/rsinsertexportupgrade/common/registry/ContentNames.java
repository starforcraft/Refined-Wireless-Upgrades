package com.ultramega.rsinsertexportupgrade.common.registry;

import net.minecraft.network.chat.MutableComponent;

import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.createInsertExportTranslation;

public final class ContentNames {
    public static final MutableComponent EXPORT_UPGRADE = createInsertExportTranslation("item", "export_upgrade");

    private ContentNames() {
    }
}
