package com.ultramega.refinedwirelessupgrades.neoforge.datagen;

import com.ultramega.refinedwirelessupgrades.common.registry.Items;

import java.util.stream.Stream;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static com.ultramega.refinedwirelessupgrades.common.util.InsertExportIdentifierUtil.MOD_ID;

public class ModelProviders extends ModelProvider {
    public ModelProviders(final PackOutput output) {
        super(output, MOD_ID);
    }

    @Override
    protected void registerModels(final BlockModelGenerators blockModels, final ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(Items.INSTANCE.getInsertUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getExportUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getBlockPickerUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getMagnetUpgrade(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Items.INSTANCE.getEnergyCapacityUpgrade(), ModelTemplates.FLAT_ITEM);
    }

    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.of();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return Stream.of();
    }

}
