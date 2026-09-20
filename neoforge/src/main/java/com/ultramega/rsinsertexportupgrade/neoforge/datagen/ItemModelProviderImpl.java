package com.ultramega.rsinsertexportupgrade.neoforge.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.ultramega.rsinsertexportupgrade.common.registry.ContentIds.BLOCK_PICKER_UPGRADE;
import static com.ultramega.rsinsertexportupgrade.common.registry.ContentIds.ENERGY_CAPACITY_UPGRADE;
import static com.ultramega.rsinsertexportupgrade.common.registry.ContentIds.EXPORT_UPGRADE;
import static com.ultramega.rsinsertexportupgrade.common.registry.ContentIds.INSERT_UPGRADE;
import static com.ultramega.rsinsertexportupgrade.common.registry.ContentIds.MAGNET_UPGRADE;
import static com.ultramega.rsinsertexportupgrade.common.util.InsertExportIdentifierUtil.MOD_ID;

public class ItemModelProviderImpl extends ItemModelProvider {
    public ItemModelProviderImpl(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        this.basicItem(INSERT_UPGRADE);
        this.basicItem(EXPORT_UPGRADE);
        this.basicItem(BLOCK_PICKER_UPGRADE);
        this.basicItem(MAGNET_UPGRADE);
        this.basicItem(ENERGY_CAPACITY_UPGRADE);
    }
}
