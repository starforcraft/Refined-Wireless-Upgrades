package com.ultramega.refinedwirelessupgrades.neoforge.datagen;

import com.ultramega.refinedwirelessupgrades.common.registry.ContentIds;
import com.ultramega.refinedwirelessupgrades.common.registry.Items;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class RecipeProviderImpl extends RecipeProvider {
    public RecipeProviderImpl(final HolderLookup.Provider registries, final RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        final Item speedUpgrade = com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getSpeedUpgrade();
        final Item stackUpgrade = com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getStackUpgrade();

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, Items.INSTANCE.getInsertUpgrade())
            .pattern("RSR")
            .pattern("STS")
            .pattern("RSR")
            .define('S', speedUpgrade)
            .define('T', stackUpgrade)
            .define('R', net.minecraft.world.item.Items.REDSTONE_BLOCK)
            .unlockedBy(getHasName(speedUpgrade), has(speedUpgrade))
            .save(this.output, ResourceKey.create(Registries.RECIPE, ContentIds.INSERT_UPGRADE));

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, Items.INSTANCE.getExportUpgrade())
            .pattern("RSR")
            .pattern("STS")
            .pattern("RSR")
            .define('S', speedUpgrade)
            .define('T', stackUpgrade)
            .define('R', com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getQuartzEnrichedIron())
            .unlockedBy(getHasName(speedUpgrade), has(speedUpgrade))
            .save(this.output, ResourceKey.create(Registries.RECIPE, ContentIds.EXPORT_UPGRADE));

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, Items.INSTANCE.getBlockPickerUpgrade())
            .pattern("RSR")
            .pattern("STS")
            .pattern("RSR")
            .define('S', speedUpgrade)
            .define('T', stackUpgrade)
            .define('R', com.refinedmods.refinedstorage.common.content.Items.INSTANCE.getQuartzEnrichedCopper())
            .unlockedBy(getHasName(speedUpgrade), has(speedUpgrade))
            .save(this.output, ResourceKey.create(Registries.RECIPE, ContentIds.BLOCK_PICKER_UPGRADE));

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, Items.INSTANCE.getMagnetUpgrade())
            .pattern("RSR")
            .pattern("STS")
            .pattern("RSR")
            .define('R', net.minecraft.world.item.Items.IRON_INGOT)
            .define('S', speedUpgrade)
            .define('T', net.minecraft.world.item.Items.ENDER_PEARL)
            .unlockedBy(getHasName(speedUpgrade), has(speedUpgrade))
            .save(this.output, ResourceKey.create(Registries.RECIPE, ContentIds.MAGNET_UPGRADE));

        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, Items.INSTANCE.getEnergyCapacityUpgrade())
            .pattern(" R ")
            .pattern("RUR")
            .pattern(" R ")
            .define('R', net.minecraft.world.item.Items.REDSTONE_BLOCK)
            .define('U', stackUpgrade)
            .unlockedBy(getHasName(stackUpgrade), has(stackUpgrade))
            .save(this.output, ResourceKey.create(Registries.RECIPE, ContentIds.ENERGY_CAPACITY_UPGRADE));
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(final PackOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(final HolderLookup.Provider registries, final RecipeOutput output) {
            return new RecipeProviderImpl(registries, output);
        }

        @Override
        public String getName() {
            return "Refined Wireless Upgrades recipes";
        }
    }
}
