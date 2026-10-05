package com.ultramega.refinedwirelessupgrades.common.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public final class InsertExportIdentifierUtil {
    public static final String MOD_ID = "rsinsertexportupgrade";

    private InsertExportIdentifierUtil() {
    }

    public static Identifier createInsertExportIdentifier(final String value) {
        return Identifier.fromNamespaceAndPath(MOD_ID, value);
    }

    public static MutableComponent createInsertExportTranslation(final String category, final String value) {
        return Component.translatable(createInsertExportTranslationKey(category, value));
    }

    public static String createInsertExportTranslationKey(final String category, final String value) {
        return String.format("%s.%s.%s", category, MOD_ID, value);
    }
}
