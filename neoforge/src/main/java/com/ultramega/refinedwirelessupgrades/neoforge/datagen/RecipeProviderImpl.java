package com.ultramega.refinedwirelessupgrades.neoforge.datagen;

import com.ultramega.refinedwirelessupgrades.common.registry.ContentIds;
import com.ultramega.refinedwirelessupgrades.common.registry.Items;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;

public final class RecipeProviderImpl extends RecipeProvider {
    public RecipeProviderImpl(final PackOutput output,
                              final CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(final RecipeOutput recipeOutput) {
        final Item speedUpgrade = com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getSpeedUpgrade();
        final Item stackUpgrade = com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getStackUpgrade();

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.INSTANCE.getInsertUpgrade())
            .pattern("RSR")
            .pattern("STS")
            .pattern("RSR")
            .define('S', speedUpgrade)
            .define('T', stackUpgrade)
            .define('R', net.minecraft.world.item.Items.REDSTONE_BLOCK)
            .unlockedBy(getHasName(speedUpgrade), has(speedUpgrade))
            .save(recipeOutput, ContentIds.INSERT_UPGRADE);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.INSTANCE.getExportUpgrade())
            .pattern("RSR")
            .pattern("STS")
            .pattern("RSR")
            .define('S', speedUpgrade)
            .define('T', stackUpgrade)
            .define('R', com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getQuartzEnrichedIron())
            .unlockedBy(getHasName(speedUpgrade), has(speedUpgrade))
            .save(recipeOutput, ContentIds.EXPORT_UPGRADE);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.INSTANCE.getBlockPickerUpgrade())
            .pattern("RSR")
            .pattern("STS")
            .pattern("RSR")
            .define('S', speedUpgrade)
            .define('T', stackUpgrade)
            .define('R', com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getQuartzEnrichedCopper())
            .unlockedBy(getHasName(speedUpgrade), has(speedUpgrade))
            .save(recipeOutput, ContentIds.BLOCK_PICKER_UPGRADE);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.INSTANCE.getMagnetUpgrade())
            .pattern("RSR")
            .pattern("STS")
            .pattern("RSR")
            .define('R', net.minecraft.world.item.Items.IRON_INGOT)
            .define('S', speedUpgrade)
            .define('T', net.minecraft.world.item.Items.ENDER_PEARL)
            .unlockedBy(getHasName(speedUpgrade), has(speedUpgrade))
            .save(recipeOutput, ContentIds.MAGNET_UPGRADE);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.INSTANCE.getEnergyCapacityUpgrade())
            .pattern(" R ")
            .pattern("RUR")
            .pattern(" R ")
            .define('R', net.minecraft.world.item.Items.REDSTONE_BLOCK)
            .define('U', stackUpgrade)
            .unlockedBy(getHasName(stackUpgrade), has(stackUpgrade))
            .save(recipeOutput, ContentIds.ENERGY_CAPACITY_UPGRADE);
    }
}
